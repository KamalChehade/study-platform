import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import axios from 'axios'
import Layout from '../components/Layout'
import FileUpload from '../components/FileUpload'

function StudyGroupDetails() {
	const { groupID } = useParams()
	const [group, setGroup] = useState(null)
	const [members, setMembers] = useState([])
	const [materials, setMaterials] = useState([])
	const [chatMessages, setChatMessages] = useState([])
	const [newMessage, setNewMessage] = useState('')
	const user = JSON.parse(localStorage.getItem('user'))

	useEffect(() => {
		if (!groupID) return

		// ✅ Fetch Group Details
		axios
			.get(`http://localhost:5000/api/study-groups/details/${groupID}`)
			.then((res) => setGroup(res.data))
			.catch(() => console.error('❌ Failed to load study group details'))

		// ✅ Fetch Study Materials
		axios
			.get(`http://localhost:5000/api/study-materials/${groupID}`)
			.then((res) => setMaterials(res.data))
			.catch(() => console.error('❌ Failed to load study materials'))

		// ✅ Fetch Group Members
		axios
			.get(`http://localhost:5000/api/study-groups/members/${groupID}`)
			.then((res) => setMembers(res.data))
			.catch(() => console.error('❌ Failed to load group members'))

		// ✅ Fetch Chat Messages
		axios
			.get(`http://localhost:5000/api/study-groups/chat/${groupID}`)
			.then((res) => setChatMessages(res.data))
			.catch(() => console.error('❌ Failed to load chat messages'))
	}, [groupID])

	// ✅ Handle Chat Message Sending
	const handleSendMessage = () => {
		if (!newMessage.trim()) return

		axios
			.post(`http://localhost:5000/api/study-groups/chat/send`, {
				groupID,
				userID: user.userID,
				message: newMessage,
			})
			.then((res) => {
				setChatMessages([...chatMessages, res.data])
				setNewMessage('')
			})
			.catch(() => console.error('❌ Failed to send message'))
	}

	// ✅ Handle Study Material Deletion
	const handleDeleteMaterial = (materialID) => {
		axios
			.delete(`http://localhost:5000/api/study-materials/delete/${materialID}`)
			.then(() => {
				setMaterials(materials.filter((mat) => mat.materialID !== materialID))
			})
			.catch(() => console.error('❌ Failed to delete study material'))
	}

	// ✅ Handle Removing a Student from Study Group (Only for Teachers)
	const handleRemoveStudent = (universityID) => {
		axios
			.delete('http://localhost:5000/api/study-groups/remove-member', {
				data: { groupID, universityID }, // Ensure correct request body
				headers: {
					'Content-Type': 'application/json', // Ensure JSON format
				},
			})
			.then(() => {
				setMembers(members.filter((m) => m.universityID !== universityID))
			})
			.catch((err) => {
				console.error('❌ Failed to remove student:', err.response?.data || err)
			})
	}

	return (
		<Layout>
			{group ? (
				<div className="grid grid-cols-1 md:grid-cols-2 gap-6">
					{/* ✅ Left Side - Study Materials & Info */}
					<div className="p-4 bg-white shadow rounded">
						<h1 className="text-3xl font-bold">{group.groupName}</h1>
						<p className="text-gray-600">{group.description}</p>

						{/* ✅ Study Materials Section */}
						<h2 className="text-xl font-semibold mt-6">Study Materials</h2>
						{user.role === 'Teacher' && (
							<FileUpload groupID={groupID} setMaterials={setMaterials} />
						)}
						<ul className="mt-2">
							{materials.map((material) => (
								<li
									key={material.materialID}
									className="p-2 border-b flex justify-between items-center"
								>
									<a
										href={material.fileLink}
										download
										className="text-blue-500"
									>
										{material.title}
									</a>
									{user.role === 'Teacher' && (
										<button
											onClick={() => handleDeleteMaterial(material.materialID)}
											className="bg-red-500 text-white px-2 py-1 rounded"
										>
											Delete
										</button>
									)}
								</li>
							))}
						</ul>
					</div>

					{/* ✅ Right Side - Members & Chat */}
					<div className="p-4 bg-white shadow rounded">
						{/* ✅ Members Section (Only for Teacher) */}
						{user.role === 'Teacher' && (
							<>
								<h2 className="text-xl font-semibold">Members</h2>
								{members.length > 0 ? (
									<ul className="border rounded p-2 mt-2 bg-gray-50">
										{/* ✅ Members Section */}
										<h2 className="text-xl font-semibold">Members</h2>
										{members.length > 0 ? (
											<ul className="border rounded p-2 mt-2 bg-gray-50">
												{members.map((member) => (
													<li
														key={member.universityID}
														className="flex justify-between p-2 border-b"
													>
														<span>
															{member.name} ({member.role}) -{' '}
															{member.universityID}
														</span>
														{user.role === 'Teacher' && (
															<button
																onClick={() =>
																	handleRemoveStudent(member.universityID)
																}
																className="text-red-500"
															>
																Remove
															</button>
														)}
													</li>
												))}
											</ul>
										) : (
											<p className="text-gray-500 mt-2">
												No students enrolled.
											</p>
										)}
									</ul>
								) : (
									<p className="text-gray-500 mt-2">No students enrolled.</p>
								)}
							</>
						)}

						{/* ✅ Chat Section */}
						<h2 className="text-xl font-semibold mt-6">Chat</h2>
						<div className="border p-4 mt-2 bg-gray-100 rounded">
							<ul className="h-40 overflow-y-auto">
								{chatMessages.map((msg) => (
									<li key={msg.messageID} className="p-2 border-b">
										<strong>{msg.name}:</strong> {msg.message}
									</li>
								))}
							</ul>
							<div className="flex gap-2 mt-2">
								<input
									type="text"
									value={newMessage}
									onChange={(e) => setNewMessage(e.target.value)}
									className="border p-2 w-full rounded"
									placeholder="Type a message..."
								/>
								<button
									onClick={handleSendMessage}
									className="bg-blue-500 text-white px-4 py-2 rounded"
								>
									Send
								</button>
							</div>
						</div>
					</div>
				</div>
			) : (
				<p className="text-gray-500">Loading...</p>
			)}
		</Layout>
	)
}

export default StudyGroupDetails
