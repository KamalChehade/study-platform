import { useEffect, useState } from 'react'
import axios from 'axios'
import { motion, AnimatePresence } from 'framer-motion'
import Layout from '../components/Layout'

function Courses() {
	const [enrolledCourses, setEnrolledCourses] = useState([])
	const [availableCourses, setAvailableCourses] = useState([])
	const user = JSON.parse(localStorage.getItem('user'))

	useEffect(() => {
		if (user?.role !== 'Student') return

		axios
			.get(`http://localhost:5000/api/courses/enrolled/${user.userID}`)
			.then((response) => {
				setEnrolledCourses(response.data)
			})
			.catch(() => console.error('Error fetching enrolled courses'))

		axios
			.get('http://localhost:5000/api/courses')
			.then((response) => {
				const allCourses = response.data
				const enrolledIDs = new Set(
					enrolledCourses.map((course) => course.courseID)
				)
				const available = allCourses.filter(
					(course) => !enrolledIDs.has(course.courseID)
				)
				setAvailableCourses(available)
			})
			.catch(() => console.error('Error fetching courses'))
	}, [user?.userID, user?.role, enrolledCourses])

	const handleEnroll = (courseID) => {
		axios
			.post('http://localhost:5000/api/courses/enroll', {
				studentID: user.userID,
				courseID,
			})
			.then(() => {
				const course = availableCourses.find((c) => c.courseID === courseID)
				setEnrolledCourses([...enrolledCourses, course])
				setAvailableCourses(
					availableCourses.filter((c) => c.courseID !== courseID)
				)
			})
			.catch(() => console.log('Failed to enroll!'))
	}

	const handleLeave = (courseID) => {
		axios
			.post('http://localhost:5000/api/courses/leave', {
				studentID: user.userID,
				courseID,
			})
			.then(() => {
				const course = enrolledCourses.find((c) => c.courseID === courseID)
				setAvailableCourses([...availableCourses, course])
				setEnrolledCourses(
					enrolledCourses.filter((c) => c.courseID !== courseID)
				)
			})
			.catch(() => console.log('Failed to leave the course!'))
	}

	if (user?.role !== 'Student') {
		return (
			<Layout>
				<p className="text-red-500">
					Access Denied: Only students can view this page.
				</p>
			</Layout>
		)
	}

	return (
		<Layout>
			<h1 className="text-3xl font-bold">Courses</h1>

			<h2 className="text-2xl font-semibold mt-6">Enrolled Courses</h2>
			<div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
				<AnimatePresence>
					{enrolledCourses.length === 0 ? (
						<p>No enrolled courses.</p>
					) : (
						enrolledCourses.map((course) => (
							<motion.div
								key={course.courseID}
								initial={{ opacity: 0, y: -10 }}
								animate={{ opacity: 1, y: 0 }}
								exit={{ opacity: 0, y: -10 }}
								transition={{ duration: 0.3 }}
								className="p-4 border rounded shadow bg-white flex justify-between items-center"
							>
								<h3 className="font-bold">{course.courseName}</h3>
								<button
									onClick={() => handleLeave(course.courseID)}
									className="px-4 py-2 bg-red-500 text-white rounded"
								>
									Leave Course
								</button>
							</motion.div>
						))
					)}
				</AnimatePresence>
			</div>

			<h2 className="text-2xl font-semibold mt-6">Available Courses</h2>
			<div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
				<AnimatePresence>
					{availableCourses.length === 0 ? (
						<p>No available courses.</p>
					) : (
						availableCourses.map((course) => (
							<motion.div
								key={course.courseID}
								initial={{ opacity: 0, y: 10 }}
								animate={{ opacity: 1, y: 0 }}
								exit={{ opacity: 0, y: 10 }}
								transition={{ duration: 0.3 }}
								className="p-4 border rounded shadow bg-white flex justify-between items-center"
							>
								<h3 className="font-bold">{course.courseName}</h3>
								<button
									onClick={() => handleEnroll(course.courseID)}
									className="px-4 py-2 bg-green-500 text-white rounded"
								>
									Enroll
								</button>
							</motion.div>
						))
					)}
				</AnimatePresence>
			</div>
		</Layout>
	)
}

export default Courses
