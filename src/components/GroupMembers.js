import axios from 'axios'

function GroupMembers({ students, user, groupID, setStudents }) {
	const handleRemove = (studentID) => {
		axios
			.delete(`${process.env.REACT_APP_API_URL}/study-groups/remove-member`, {
				data: { groupID, studentID },
			})
			.then(() => {
				setStudents((prev) =>
					prev.filter((student) => student.userID !== studentID)
				)
			})
			.catch(() => console.error('❌ Failed to remove student'))
	}

	return (
		<ul className="mt-2">
			{students.map((student) => (
				<li key={student.userID} className="border-b p-2 flex justify-between">
					<span>{student.name}</span>
					{user.role === 'Teacher' && (
						<button
							onClick={() => handleRemove(student.userID)}
							className="text-red-500"
						>
							Remove
						</button>
					)}
				</li>
			))}
		</ul>
	)
}

export default GroupMembers
