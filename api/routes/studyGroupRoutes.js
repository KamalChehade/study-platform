const express = require('express')
const db = require('../config/db')
const multer = require('multer')
const path = require('path')

const router = express.Router()

// ✅ Configure File Upload (Multer)
const storage = multer.diskStorage({
	destination: './uploads/',
	filename: (req, file, cb) => {
		cb(null, Date.now() + path.extname(file.originalname))
	},
})
const upload = multer({ storage })

// ✅ Get All Study Groups
router.get('/', (req, res) => {
	db.query('SELECT * FROM studygroups', (err, results) => {
		if (err) {
			console.error('❌ Error fetching study groups:', err)
			return res.status(500).json({ error: 'Failed to fetch study groups' })
		}
		res.json(results)
	})
})

// ✅ Get Study Group Details
router.get('/details/:groupID', (req, res) => {
	const { groupID } = req.params
	db.query(
		'SELECT * FROM studygroups WHERE groupID = ?',
		[groupID],
		(err, result) => {
			if (err)
				return res
					.status(500)
					.json({ error: 'Failed to fetch study group details' })
			if (result.length === 0)
				return res.status(404).json({ error: 'Study group not found' })
			res.json(result[0])
		}
	)
})

// ✅ Add a New Study Group
router.post('/add', (req, res) => {
	const { groupName, courseID, description, teacherID } = req.body
	if (!groupName || !courseID || !description || !teacherID) {
		return res.status(400).json({ error: 'Missing required fields' })
	}

	const sql = `INSERT INTO studygroups (groupName, courseID, description, teacherID, createdAt, updatedAt)
	             VALUES (?, ?, ?, ?, NOW(), NOW())`

	db.query(
		sql,
		[groupName, courseID, description, teacherID],
		(err, result) => {
			if (err) {
				console.error('❌ Database error:', err)
				return res.status(500).json({
					error: 'Failed to create study group',
					details: err.sqlMessage,
				})
			}
			res.json({
				groupID: result.insertId,
			})
		}
	)
})

// ✅ Delete a Study Group
router.delete('/delete/:id', (req, res) => {
	const { id } = req.params
	db.query('DELETE FROM studygroups WHERE groupID = ?', [id], (err, result) => {
		if (err) {
			console.error('❌ Error deleting study group:', err)
			return res.status(500).json({ error: 'Failed to delete study group' })
		}
		res.json({})
	})
})

// ✅ Get Study Groups for a User (Teacher or Student)
router.get('/:userID', (req, res) => {
	const { userID } = req.params

	db.query(
		'SELECT role, universityID FROM users WHERE userID = ?',
		[userID],
		(err, userResult) => {
			if (err) {
				console.error('❌ Error fetching user role:', err)
				return res.status(500).json({ error: 'Server error' })
			}
			if (userResult.length === 0) {
				return res.status(404).json({ error: 'User not found' })
			}

			const { role, universityID } = userResult[0]

			let sql
			let params

			if (role === 'Teacher') {
				sql = 'SELECT * FROM studygroups WHERE teacherID = ?'
				params = [universityID]
			} else {
				sql = `SELECT sg.* FROM studygroups sg
			       JOIN enrollments e ON sg.courseID = e.courseID
			       WHERE e.studentID = ?`
				params = [userID]
			}

			db.query(sql, params, (err, results) => {
				if (err) {
					console.error('❌ Error fetching study groups:', err)
					return res.status(500).json({ error: 'Failed to load study groups' })
				}
				res.json(results)
			})
		}
	)
})

// ✅ Get Study Group Members
router.get('/members/:groupID', (req, res) => {
	const { groupID } = req.params

	const sql = `
        SELECT u.userID, u.name, u.role 
        FROM studygroupmembers sgm
        JOIN users u ON sgm.universityID = u.userID
        WHERE sgm.groupID = ?
    `

	db.query(sql, [groupID], (err, result) => {
		if (err) {
			console.error('❌ Error fetching group members:', err)
			return res.status(500).json({ error: 'Failed to fetch group members' })
		}

		res.json(result)
	})
})

// ✅ Remove a Student from Study Group (For Teachers)
router.delete('/remove-member', (req, res) => {
	// Use req.body to get the data
	const { groupID, universityID } = req.body

	if (!groupID || !universityID) {
		return res
			.status(400)
			.json({ error: 'Missing required fields: groupID or universityID' })
	}

	const sql = `DELETE FROM studygroupmembers WHERE groupID = ? AND universityID = ?`

	db.query(sql, [groupID, universityID], (err, result) => {
		if (err) {
			console.error('❌ Error removing student from study group:', err)
			return res.status(500).json({ error: 'Failed to remove student' })
		}

		if (result.affectedRows === 0) {
			return res.status(404).json({ error: 'Student not found in study group' })
		}

		res.json()
	})
})

// ✅ Upload Study Materials
router.post('/upload-materials', upload.array('files', 5), (req, res) => {
	const { groupID, teacherID } = req.body
	if (!req.files || req.files.length === 0)
		return res.status(400).json({ error: 'No files uploaded' })

	const materials = req.files.map((file) => [
		groupID,
		teacherID,
		file.originalname,
		`/uploads/${file.filename}`,
		new Date(),
		new Date(),
	])

	const sql = `INSERT INTO studymaterials (groupID, teacherID, title, fileLink, uploadedAt, createdAt, updatedAt) VALUES ?`

	db.query(sql, [materials], (err, result) => {
		if (err) {
			console.error('❌ Error uploading files:', err)
			return res.status(500).json({ error: 'Failed to upload study materials' })
		}
		res.json({})
	})
})

// ✅ Get Study Materials
router.get('/materials/:groupID', (req, res) => {
	const { groupID } = req.params
	const sql = 'SELECT * FROM studymaterials WHERE groupID = ?'

	db.query(sql, [groupID], (err, result) => {
		if (err)
			return res.status(500).json({ error: 'Failed to fetch study materials' })
		res.json(result)
	})
})

// ✅ Delete Study Material
router.delete('/materials/delete/:materialID', (req, res) => {
	const { materialID } = req.params
	const sql = 'DELETE FROM studymaterials WHERE materialID = ?'

	db.query(sql, [materialID], (err, result) => {
		if (err)
			return res.status(500).json({ error: 'Failed to delete study material' })
		res.json({})
	})
})

// ✅ Fetch Chat Messages
router.get('/chat/:groupID', (req, res) => {
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

// ✅ Send Chat Message
router.post('/chat/send', (req, res) => {
	const { groupID, userID, message } = req.body
	const sql = `INSERT INTO chatmessages (groupID, userID, message, timestamp, createdAt, updatedAt)
	             VALUES (?, ?, ?, NOW(), NOW(), NOW())`

	db.query(sql, [groupID, userID, message], (err, result) => {
		if (err) return res.status(500).json({ error: 'Failed to send message' })
		res.json({ messageID: result.insertId, userID, groupID, message })
	})
})

module.exports = router
