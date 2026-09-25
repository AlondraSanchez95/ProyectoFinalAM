require('dotenv').config();
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');
const admin = require('firebase-admin');
const belvo = require('belvo').default;

const app = express();
const PORT = process.env.PORT || 3000;

app.use(helmet());
app.disable('x-powered-by');

const corsOptions = {
  origin: process.env.ALLOWED_ORIGIN || '*', 
  methods: ['GET', 'POST'],
  allowedHeaders: ['Content-Type', 'Authorization']
};
app.use(cors(corsOptions));

app.use(express.json({ limit: '10kb' }));

const globalLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 100,
  standardHeaders: true,
  legacyHeaders: false,
  message: {
    success: false,
    message: 'Demasiadas peticiones realizadas desde esta IP. Intenta de nuevo más tarde.'
  }
});
app.use('/api/', globalLimiter);

const widgetTokenLimiter = rateLimit({
  windowMs: 60 * 60 * 1000, // 1 hora
  max: 20, // Máximo 20 tokens por hora por IP
  message: {
    success: false,
    message: 'Límite de generación de tokens excedido.'
  }
});

if (process.env.FIREBASE_SERVICE_ACCOUNT) {
  try {
    const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount)
    });
    console.log('Firebase Admin SDK autenticado correctamente.');
  } catch (error) {
    console.error('Error al parsear FIREBASE_SERVICE_ACCOUNT:', error.message);
  }
} else {
  console.warn('FIREBASE_SERVICE_ACCOUNT no configurada. La verificación de tokens está inactiva.');
}

const belvoClient = new belvo(
  process.env.BELVO_SECRET_ID,
  process.env.BELVO_SECRET_PASSWORD,
  process.env.BELVO_ENV || 'sandbox'
);

async function ensureBelvoConnection() {
  try {
    await belvoClient.connect();
  } catch (error) {
    console.error('Error de conexión con la API de Belvo:', error.message);
  }
}

async function authenticateFirebaseUser(req, res, next) {
  const authHeader = req.headers.authorization;

  if (!process.env.FIREBASE_SERVICE_ACCOUNT) {
    return next();
  }

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({
      success: false,
      message: 'Acceso denegado: Token de autenticación ausente o inválido.'
    });
  }

  const idToken = authHeader.split('Bearer ')[1];

  try {
    const decodedToken = await admin.auth().verifyIdToken(idToken);
    req.user = decodedToken; 
    next();
  } catch (error) {
    console.error('Error al verificar Token de Firebase:', error.message);
    return res.status(403).json({
      success: false,
      message: 'Token de sesión expirado o no válido.'
    });
  }
}

app.get('/api/health', (req, res) => {
  res.json({
    status: 'online',
    environment: process.env.BELVO_ENV || 'sandbox',
    timestamp: new Date().toISOString()
  });
});

app.post('/api/belvo/access-token', widgetTokenLimiter, authenticateFirebaseUser, async (req, res) => {
  try {
    await ensureBelvoConnection();
    const response = await belvoClient.widgetAccessTokens.create();

    res.json({
      success: true,
      access_token: response.access_token
    });
  } catch (error) {
    console.error('Error al generar Widget Access Token:', error.message);
    res.status(500).json({
      success: false,
      message: 'No se pudo generar el token del widget.'
    });
  }
});

app.post('/api/belvo/accounts', authenticateFirebaseUser, async (req, res) => {
  const { link_id } = req.body;

  if (!link_id || typeof link_id !== 'string' || link_id.trim() === '') {
    return res.status(400).json({
      success: false,
      message: 'El parámetro "link_id" es inválido o está ausente.'
    });
  }

  try {
    await ensureBelvoConnection();
    const accounts = await belvoClient.accounts.retrieve(link_id);

    const formattedAccounts = accounts.map((acc) => ({
      id: acc.id,
      name: acc.name,
      type: acc.type,
      category: acc.category,
      currency: acc.currency,
      balanceCurrent: acc.balance ? acc.balance.current : 0,
      balanceAvailable: acc.balance ? acc.balance.available : 0,
      number: acc.number ? `**** ${acc.number.slice(-4)}` : '****'
    }));

    res.json({
      success: true,
      count: formattedAccounts.length,
      accounts: formattedAccounts
    });
  } catch (error) {
    console.error('Error recuperando cuentas:', error.message);
    res.status(500).json({
      success: false,
      message: 'Error al consultar la información financiera.'
    });
  }
});


app.post('/api/belvo/transactions', authenticateFirebaseUser, async (req, res) => {
  const { link_id, dateFrom, dateTo } = req.body;

  if (!link_id || typeof link_id !== 'string') {
    return res.status(400).json({
      success: false,
      message: 'El parámetro "link_id" es requerido.'
    });
  }

  try {
    await ensureBelvoConnection();

    const today = new Date().toISOString().split('T')[0];
    const thirtyDaysAgo = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
      .toISOString()
      .split('T')[0];

    const transactions = await belvoClient.transactions.retrieve(
      link_id,
      dateFrom || thirtyDaysAgo,
      { dateTo: dateTo || today }
    );

    const formattedTransactions = transactions.map((tx) => ({
      id: tx.id,
      account: tx.account ? tx.account.id : null,
      amount: tx.amount,
      type: tx.type,
      status: tx.status,
      description: tx.description,
      category: tx.category || 'Otros',
      valueDate: tx.value_date
    }));

    res.json({
      success: true,
      count: formattedTransactions.length,
      transactions: formattedTransactions
    });
  } catch (error) {
    console.error('Error recuperando transacciones:', error.message);
    res.status(500).json({
      success: false,
      message: 'Error al consultar las transacciones.'
    });
  }
});


app.listen(PORT, () => {
  console.log(`Servidor seguro activo en puerto ${PORT}`);
});