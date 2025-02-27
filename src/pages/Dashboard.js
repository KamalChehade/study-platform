import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Layout from '../components/Layout'

function Dashboard() {
	const [user, setUser] = useState(null)
	const navigate = useNavigate()

	useEffect(() => {
		const storedUser = JSON.parse(localStorage.getItem('user'))
		if (!storedUser) {
			navigate('/signin')
		} else {
			setUser(storedUser)
		}
	}, [navigate])

	return (
		<Layout>
			<div className="flex flex-col items-center justify-center min-h-screen">
				<h1 className="text-3xl font-bold">
					Welcome Back, {user?.name || 'Teacher'}!
				</h1>
				<p className="text-gray-700 mt-2">
					Your Role: {''}
					<span className="font-semibold">{user?.role}</span>
				</p>
			</div>
		</Layout>
	)
}

export default Dashboard
