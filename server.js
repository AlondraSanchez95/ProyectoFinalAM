require('dotenv').config();
const express = require('express');
const cors = require('cors');
const belvo = require('belvo').default;
const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

const belvoClient = new belvo(
  process.env.BELVO_SECRET_ID,
  process.env.BELVO_SECRET_PASSWORD,
  process.env.BELVO_ENV || 'sandbox'
);

async function ensureBelvoConnection() {
  try {
    await belvoClient.connect();
  } catch (error) {
    console.error('Error autenticando cliente con la API de Belvo:', error.message);
  }
}

app.get('/api/health', (req, res) => {
  res.json({
    status: 'online',
    environment: process.env.BELVO_ENV || 'sandbox',
    timestamp: new Date().toISOString()
  });
});

// 2. Generar Access Token para el Widget de Belvo en Android
app.post('/api/belvo/access-token', async (req, res) => {
  try {
    await ensureBelvoConnection();
    const response = await belvoClient.widgetAccessTokens.create();
    
    res.json({
      success: true,
      access_token: response.access_token
    });
  } catch (error) {
    console.error('Error al generar Widget Access Token:', error);
    res.status(500).json({
      success: false,
      message: 'Error al generar el token de acceso al widget',
      error: error.message
    });
  }
});

app.post('/api/belvo/accounts', async (req, res) => {
  const { link_id } = req.body;

  if (!link_id) {
    return res.status(400).json({
      success: false,
      message: 'El parámetro "link_id" es obligatorio.'
    });
  }

  try {
    await ensureBelvoConnection();
    const accounts = await belvoClient.accounts.retrieve(link_id);
    const formattedAccounts = accounts.map((acc) => ({
      id: acc.id,
      name: acc.name,
      type: acc.type, // RECEIVING_ACCOUNT, CREDIT_CARD, etc.
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
    console.error('Error recuperando cuentas:', error);
    res.status(500).json({
      success: false,
      message: 'Error al consultar las cuentas bancarias',
      error: error.message
    });
  }
});

app.post('/api/belvo/transactions', async (req, res) => {
  const { link_id, dateFrom, dateTo } = req.body;

  if (!link_id) {
    return res.status(400).json({
      success: false,
      message: 'El parámetro "link_id" es obligatorio.'
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
      type: tx.type, // INFLOW (Depósito/Entrada) o OUTFLOW (Gasto/Salida)
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
    console.error('Error recuperando transacciones:', error);
    res.status(500).json({
      success: false,
      message: 'Error al consultar el historial de transacciones',
      error: error.message
    });
  }
});

// Arrancar servidor
app.listen(PORT, () => {
  console.log(`Servidor backend activo en el puerto ${PORT}`);
  console.log(`Entorno Belvo configurado: ${process.env.BELVO_ENV || 'sandbox'}`);
});