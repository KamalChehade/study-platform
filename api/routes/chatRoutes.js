const express = require('express')
const db = require('../config/db')
const router = express.Router()

// ✅ Fetch Chat Messages
router.get('/:groupID', (req, res) => {
	const { groupID } = req.params
	const sql = `SELECT chatmessages.*, users.name FROM chatmessages 
				 JOIN users ON chatmessages.userID = users.userID 
				 WHERE groupID = ? ORDER BY timestamp ASC`
	db.query(sql, [groupID], (err, result) => {
		if (err)
			return res.status(500).json({ error: 'Failed to fetch chat messages' })
		res.json(result)
	})
})

// ✅ Send Message
router.post('/send', (req, res) => {
	const { groupID, userID, message } = req.body
	const sql = `INSERT INTO chatmessages (groupID, userID, message, timestamp, createdAt, updatedAt)
				 VALUES (?, ?, ?, NOW(), NOW(), NOW())`
	db.query(sql, [groupID, userID, message], (err, result) => {
		if (err) return res.status(500).json({ error: 'Failed to send message' })
		res.json({ messageID: result.insertId, userID, groupID, message })
	})
})

module.exports = router
