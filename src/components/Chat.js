import { useState, useEffect } from 'react'
import axios from 'axios'

function Chat({ groupID, user }) {
	const [messages, setMessages] = useState([])
	const [message, setMessage] = useState('')

	useEffect(() => {
		axios
			.get(`${process.env.REACT_APP_API_URL}/chat/${groupID}`)
			.then((res) => setMessages(res.data))
			.catch(() => console.error('❌ Failed to fetch chat messages'))
	}, [groupID])

	const sendMessage = () => {
		if (!message.trim()) return

		axios
			.post(`${process.env.REACT_APP_API_URL}/chat/send`, {
				groupID,
				userID: user.userID,
				message,
			})
			.then((res) => {
				setMessages([...messages, res.data])
				setMessage('')
			})
			.catch(() => console.error('❌ Failed to send message'))
	}

	return (
		<div>
			<div className="h-48 overflow-auto border p-2">
				{messages.map((msg, index) => (
					<p key={index} className="text-gray-700">
						<strong>{msg.name}:</strong> {msg.message}
					</p>
				))}
			</div>
			<input
				type="text"
				value={message}
				onChange={(e) => setMessage(e.target.value)}
				className="border p-2 w-full mt-2"
			/>
			<button
				onClick={sendMessage}
				className="px-4 py-2 bg-blue-500 text-white rounded mt-2"
			>
				Send
			</button>
		</div>
	)
}

export default Chat
