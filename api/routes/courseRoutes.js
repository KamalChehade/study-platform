const express = require('express')
const db = require('../config/db')

const router = express.Router()

// ✅ Get all courses
router.get('/', (req, res) => {
	const sql = 'SELECT * FROM courses'
	db.query(sql, (err, results) => {
		if (err) {
			console.error('Error fetching courses:', err)
			return res.status(500).json({ error: 'Failed to retrieve courses' })
		}
		res.json(results)
	})
})

// ✅ Assign a teacher to a course (Manual Assignment)
router.post('/assign-teacher', (req, res) => {
	const { courseID, teacherID } = req.body

	if (!courseID || !teacherID) {
		return res.status(400).json({ error: 'Missing required fields' })
	}

	const sql =
		'UPDATE courses SET teacherID = ? WHERE LOWER(courseID) = LOWER(?)'
	db.query(sql, [teacherID, courseID], (err, result) => {
		if (err) {
			console.error('Error assigning teacher:', err)
			return res
				.status(500)
				.json({ error: 'Failed to assign teacher to course' })
		}
		if (result.affectedRows === 0) {
			return res.status(404).json({ error: 'Course not found' })
		}
		res.json({  })
	})
})

router.get('/enrolled/:studentID', (req, res) => {
	const studentID = req.params.studentID
	const sql = `
      SELECT courses.* FROM enrollments 
      JOIN courses ON enrollments.courseID = courses.courseID 
      WHERE enrollments.studentID = ?`

	db.query(sql, [studentID], (err, results) => {
		if (err) {
			console.error('Error fetching enrolled courses:', err)
			return res
				.status(500)
				.json({ error: 'Failed to retrieve enrolled courses' })
		}
		res.json(results)
	})
})

router.post('/enroll', (req, res) => {
	const { studentID, courseID } = req.body

	if (!studentID || !courseID) {
		return res.status(400).json({ error: 'Missing required fields' })
	}

	// Find study groups linked to this course
	const getStudyGroups = `SELECT groupID FROM studygroups WHERE courseID = ?`

	db.query(getStudyGroups, [courseID], (err, groups) => {
		if (err) {
			console.error('❌ Error finding study groups:', err)
			return res.status(500).json({ error: 'Failed to find study groups' })
		}

		// Insert student into enrollments table
		const enrollSQL = `INSERT INTO enrollments (studentID, courseID, enrollmentDate, createdAt, updatedAt) VALUES (?, ?, NOW(), NOW(), NOW())`

		db.query(enrollSQL, [studentID, courseID], (err, enrollResult) => {
			if (err) {
				console.error('❌ Enrollment error:', err)
				return res.status(500).json({ error: 'Failed to enroll in course' })
			}

			// Insert student into all related study groups
			const addToStudyGroups = `INSERT INTO studygroupmembers (groupID, universityID, addedDate, createdAt, updatedAt) VALUES ?`

			const studyGroupValues = groups.map((group) => [
				group.groupID,
				studentID,
				new Date(),
				new Date(),
				new Date(),
			])

			if (studyGroupValues.length > 0) {
				db.query(
					addToStudyGroups,
					[studyGroupValues],
					(err, groupEnrollResult) => {
						if (err) {
							console.error('❌ Failed to add student to study groups:', err)
							return res
								.status(500)
								.json({
									error:
										'Student enrolled in course, but not added to study groups',
								})
						}

						res.json({})
					}
				)
			} else {
				res.json({})
			}
		})
	})
})

router.post('/leave', (req, res) => {
	const { studentID, courseID } = req.body

	const sql = 'DELETE FROM enrollments WHERE studentID = ? AND courseID = ?'
	db.query(sql, [studentID, courseID], (err, result) => {
		if (err) {
			console.error('Error leaving course:', err)
			return res.status(500).json({ error: 'Failed to leave the course' })
		}
		res.json({ })
	})
})

module.exports = router
