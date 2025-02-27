const express = require('express')
const multer = require('multer')
const path = require('path')
const db = require('../config/db')
const fs = require('fs')

const router = express.Router()

// ✅ Configure File Upload (Multer)
const storage = multer.diskStorage({
	destination: (req, file, cb) => {
		const uploadPath = path.join(__dirname, '../uploads/')
		cb(null, uploadPath)
	},
	filename: (req, file, cb) => {
		cb(null, Date.now() + '-' + file.originalname)
	},
})

const upload = multer({ storage })

// ✅ Upload Study Material
router.post('/upload', upload.array('files', 10), (req, res) => {
	const { groupID, teacherID } = req.body

	if (!req.files || req.files.length === 0)
		return res.status(400).json({ error: 'No files uploaded' })

	if (!groupID || !teacherID)
		return res.status(400).json({ error: 'Missing groupID or teacherID' })

	// ✅ Insert into database
	const materials = req.files.map((file) => [
		groupID, // groupID
		teacherID, // teacherID
		file.originalname, // title
		`uploads/${file.filename}`, // fileLink
		new Date(), // uploadedAt
		new Date(), // createdAt
		new Date(), // updatedAt
	])

	const sql = `INSERT INTO studymaterials (groupID, teacherID, title, fileLink, uploadedAt, createdAt, updatedAt) VALUES ?`

	db.query(sql, [materials], (err, result) => {
		if (err) {
			console.error('❌ Database Error:', err)
			return res.status(500).json({
				error: 'Failed to upload study materials',
				details: err.sqlMessage,
			})
		}

		res.json(
			materials.map((mat, index) => ({
				materialID: result.insertId + index,
				groupID,
				teacherID,
				title: mat[2],
				fileLink: mat[3],
			}))
		)
	})
})

// ✅ Get Study Materials
router.get('/:groupID', (req, res) => {
	const { groupID } = req.params
	const sql = `SELECT * FROM studymaterials WHERE groupID = ?`

	db.query(sql, [groupID], (err, result) => {
		if (err)
			return res.status(500).json({ error: 'Failed to fetch study materials' })
		res.json(result)
	})
})

router.delete('/delete/:materialID', (req, res) => {
	const { materialID } = req.params

	// Fetch file link before deleting from DB
	db.query(
		'SELECT fileLink FROM studymaterials WHERE materialID = ?',
		[materialID],
		(err, result) => {
			if (err) {
				console.error('❌ Error finding study material:', err)
				return res.status(500).json({ error: 'Failed to find study material' })
			}

			if (result.length === 0) {
				return res.status(404).json({ error: 'Study material not found' })
			}

			const filePath = path.join(__dirname, '..', result[0].fileLink)

			// Delete file from filesystem
			fs.unlink(filePath, (err) => {
				if (err && err.code !== 'ENOENT') {
					console.error('❌ Error deleting file:', err)
					return res.status(500).json({ error: 'Failed to delete file' })
				}

				// Delete from database
				db.query(
					'DELETE FROM studymaterials WHERE materialID = ?',
					[materialID],
					(err, deleteResult) => {
						if (err) {
							console.error('❌ Error deleting study material:', err)
							return res
								.status(500)
								.json({ error: 'Failed to delete study material' })
						}

						res.json({ })
					}
				)
			})
		}
	)
})

module.exports = router
