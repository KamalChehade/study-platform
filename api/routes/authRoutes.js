const express = require('express')
const db = require('../config/db')

const router = express.Router()

// ✅ User Registration with Course Assignment
router.post('/register', (req, res) => {
	const { name, email, password, role, universityID } = req.body

	// Insert user into the database
	const sql =
		'INSERT INTO users (name, email, password, role, universityID) VALUES (?, ?, ?, ?, ?)'
	db.query(sql, [name, email, password, role, universityID], (err, result) => {
		if (err) {
			return res.status(500).json({ error: 'Registration failed' })
		}

		// If user is a teacher, check if their universityID matches a courseID
		if (role === 'Teacher') {
			const courseCheckQuery =
				'SELECT courseID FROM courses WHERE LOWER(courseID) = LOWER(?)'
			db.query(courseCheckQuery, [universityID], (courseErr, courseResult) => {
				if (courseErr) {
					return res.status(500).json({ error: 'Course check failed' })
				}

				if (courseResult.length > 0) {
					// Use the new course route to assign the teacher
					const assignCourseQuery =
						'UPDATE courses SET teacherID = ? WHERE LOWER(courseID) = LOWER(?)'
					db.query(
						assignCourseQuery,
						[universityID, universityID],
						(assignErr) => {
							if (assignErr) {
								return res
									.status(500)
									.json({ error: 'Course assignment failed' })
							}
							console.log('User registered and course assigned successfully!')
							return res.json({})
						}
					)
				} else {
					console.log('User registered, but no matching course found.')
					return res.json({})
				}
			})
		} else {
			return res.json({})
		}
	})
})

// ✅ User Login
router.post('/login', (req, res) => {
	const { email, password } = req.body
	const sql = 'SELECT * FROM users WHERE email = ? AND password = ?'
	db.query(sql, [email, password], (err, result) => {
		if (err) return res.status(500).json({ error: 'Database error' })

		if (result.length > 0) {
			res.json({ user: result[0] })
		} else {
			res.status(401).json({ error: 'Invalid email or password' })
		}
	})
})

module.exports = router
