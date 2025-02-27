import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import axios from 'axios'
import Layout from '../components/Layout'

function StudyGroups() {
	const [studyGroups, setStudyGroups] = useState([])
	const [courses, setCourses] = useState([])
	const [groupName, setGroupName] = useState('')
	const [description, setDescription] = useState('')
	const [selectedCourse, setSelectedCourse] = useState('')
	const [loading, setLoading] = useState(true)
	const [error, setError] = useState('')
	const [retryCount, setRetryCount] = useState(0)

	const user = JSON.parse(localStorage.getItem('user'))
	const navigate = useNavigate()
	const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:5000/api'

	useEffect(() => {
		if (!user) {
			navigate('/signin')
			return
		}

		// ✅ Fetch Study Groups with Retry Mechanism
		const fetchStudyGroups = async (attempt = 1) => {
			try {
				const res = await axios.get(`${API_URL}/study-groups/${user.userID}`)
				setStudyGroups(res.data)
				setLoading(false)
			} catch (err) {
				console.error(
					`❌ Attempt ${attempt} - Failed to fetch study groups`,
					err
				)
				if (attempt < 3) {
					setTimeout(() => fetchStudyGroups(attempt + 1), 1000) // Retry after 1 sec
				} else {
					setError('')
					setLoading(false)
				}
			}
		}

		// ✅ Fetch Teacher's Courses
		const fetchCourses = async () => {
			if (user.role === 'Teacher') {
				try {
					const res = await axios.get(`${API_URL}/courses`)
					const teacherCourses = res.data.filter(
						(course) => course.teacherID === parseInt(user.universityID)
					)
					setCourses(teacherCourses)
				} catch (err) {
					console.error('❌ Failed to fetch courses', err)
				}
			}
		}

		fetchStudyGroups()
		fetchCourses()
	}, [user, navigate, API_URL, retryCount])

	// ✅ Retry Button (Only shown if there was an error)
	const handleRetry = () => {
		setError('')
		setLoading(true)
		setRetryCount(retryCount + 1)
	}

	// ✅ Add Study Group
	const handleAddStudyGroup = () => {
		if (!groupName || !selectedCourse || !description) {
			console.log('Please fill all fields')
			return
		}

		axios
			.post(`${API_URL}/study-groups/add`, {
				groupName,
				courseID: selectedCourse,
				description,
				teacherID: user.universityID,
			})
			.then((res) => {
				const newGroup = {
					groupID: res.data.groupID,
					groupName,
					courseID: selectedCourse,
					description,
				}
				setStudyGroups([...studyGroups, newGroup])
				setGroupName('')
				setDescription('')
				setSelectedCourse('')
			})
			.catch((err) => {
				console.error('❌ Error:', err)
				console.log('Failed to create study group')
			})
	}

	return (
		<Layout>
			{error && (
				<div className="text-red-500 flex items-center">
					<p>{error}</p>
					<button
						onClick={handleRetry}
						className="ml-4 px-4 py-1 bg-red-500 text-white rounded"
					>
						Retry
					</button>
				</div>
			)}
			<h1 className="text-3xl font-bold">Study Groups</h1>

			{/* Error Message */}
			{error && <p className="text-red-500">{error}</p>}

			{/* Loading Indicator */}
			{loading && <p className="text-gray-500">Loading study groups...</p>}

			{/* Only show form for Teachers */}
			{user.role === 'Teacher' && (
				<div className="mt-4 p-4 bg-white shadow rounded">
					<h2 className="text-xl font-semibold">Add Study Group</h2>
					<div className="flex gap-4 mt-2">
						<input
							type="text"
							placeholder="Group Name"
							className="border p-2 rounded w-1/3"
							value={groupName}
							onChange={(e) => setGroupName(e.target.value)}
						/>
						<select
							className="border p-2 rounded w-1/3"
							value={selectedCourse}
							onChange={(e) => setSelectedCourse(e.target.value)}
						>
							<option value="">Select Course</option>
							{courses.map((course) => (
								<option key={course.courseID} value={course.courseID}>
									{course.courseName}
								</option>
							))}
						</select>
						<input
							type="text"
							placeholder="Description"
							className="border p-2 rounded w-1/3"
							value={description}
							onChange={(e) => setDescription(e.target.value)}
						/>
						<button
							onClick={handleAddStudyGroup}
							className="px-4 py-2 bg-blue-500 text-white rounded w-1/5"
						>
							Add
						</button>
					</div>
				</div>
			)}

			{/* Study Groups List */}
			<div className="mt-6">
				{studyGroups.length > 0 ? (
					studyGroups.map((group) => (
						<div
							key={group.groupID}
							className="p-4 border rounded shadow bg-white flex justify-between items-center"
						>
							<div>
								<h3 className="font-bold">{group.groupName}</h3>
								<p className="text-gray-600">{group.description}</p>
							</div>
							<button
								onClick={() => navigate(`/study-groups/${group.groupID}`)}
								className="px-4 py-2 bg-gray-500 text-white rounded"
							>
								View
							</button>
						</div>
					))
				) : (
					<p className="text-gray-500">No study groups available.</p>
				)}
			</div>
		</Layout>
	)
}

export default StudyGroups
