import sqlite3InitModule from "@sqlite.org/sqlite-wasm";

let sqlite3Promise;
let nextDatabaseId = 1;
let nextStatementId = 1;
const databases = new Map();
const statements = new Map();

const SQLITE_INTEGER = 1;
const SQLITE_FLOAT = 2;
const SQLITE_TEXT = 3;
const SQLITE_BLOB = 4;
const SQLITE_NULL = 5;

function sqlite3() {
  if (!sqlite3Promise) {
    sqlite3Promise = sqlite3InitModule();
  }
  return sqlite3Promise;
}

function databaseName(fileName) {
  return fileName.startsWith("/") ? fileName : `/${fileName}`;
}

function cellType(value) {
  if (value == null) return SQLITE_NULL;
  if (value instanceof Uint8Array || value instanceof ArrayBuffer) return SQLITE_BLOB;
  if (typeof value === "number") return Number.isInteger(value) ? SQLITE_INTEGER : SQLITE_FLOAT;
  return SQLITE_TEXT;
}

function columnNames(stmt) {
  const count = stmt.columnCount ?? 0;
  const names = [];
  for (let index = 0; index < count; index += 1) {
    names.push(stmt.getColumnName(index));
  }
  return names;
}

async function openDatabase(fileName) {
  const sqlite = await sqlite3();
  const DbClass = "opfs" in sqlite ? sqlite.oo1.OpfsDb : sqlite.oo1.DB;
  const db = new DbClass(databaseName(fileName), "ct");
  const databaseId = nextDatabaseId++;
  databases.set(databaseId, db);
  return { databaseId };
}

function prepareStatement(databaseId, sql) {
  const db = databases.get(databaseId);
  if (!db) throw new Error(`Database ${databaseId} is closed.`);
  const stmt = db.prepare(sql);
  const statementId = nextStatementId++;
  statements.set(statementId, stmt);
  return {
    statementId,
    parameterCount: stmt.parameterCount ?? 0,
    columnNames: columnNames(stmt)
  };
}

function stepStatement(statementId, bindings) {
  const stmt = statements.get(statementId);
  if (!stmt) throw new Error(`Statement ${statementId} is closed.`);

  stmt.reset();
  stmt.clearBindings();
  if (bindings?.length) {
    stmt.bind(bindings);
  }

  const rows = [];
  let columnTypes = [];
  while (stmt.step()) {
    const row = stmt.get([]);
    rows.push(row);
    if (columnTypes.length === 0) {
      columnTypes = row.map(cellType);
    }
  }

  return { rows, columnTypes };
}

function closeResource(statementId, databaseId) {
  if (statementId != null) {
    const stmt = statements.get(statementId);
    if (stmt) {
      stmt.finalize();
      statements.delete(statementId);
    }
  }
  if (databaseId != null) {
    const db = databases.get(databaseId);
    if (db) {
      db.close();
      databases.delete(databaseId);
    }
  }
}

self.onmessage = async (event) => {
  const { id, data } = event.data;
  try {
    if (data.cmd === "open") {
      self.postMessage({ id, data: await openDatabase(data.fileName) });
    } else if (data.cmd === "prepare") {
      self.postMessage({ id, data: prepareStatement(data.databaseId, data.sql) });
    } else if (data.cmd === "step") {
      self.postMessage({ id, data: stepStatement(data.statementId, data.bindings) });
    } else if (data.cmd === "close") {
      closeResource(data.statementId, data.databaseId);
    } else {
      throw new Error(`Unknown SQLite worker command: ${data.cmd}`);
    }
  } catch (error) {
    self.postMessage({ id, error: error?.message ?? String(error) });
  }
};