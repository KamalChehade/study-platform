const mysql = require('mysql2')

const db = mysql.createPool({
	host: process.env.DB_HOST || 'localhost',
	user: process.env.DB_USER || 'root',
	password: process.env.DB_PASSWORD || '',
	database: process.env.DB_NAME || 'study_platform',
	waitForConnections: true,
	connectionLimit: 10,
	queueLimit: 0,
})

db.getConnection((err, connection) => {
	if (err) {
		console.error('❌ Database connection failed:', err)
	} else {
		console.log('✅ Connected to MySQL Database')
		connection.release() // Release connection after testing
	}
})

module.exports = db
