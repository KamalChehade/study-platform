import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import axios from 'axios'

function SignIn() {
	const [email, setEmail] = useState('')
	const [password, setPassword] = useState('')
	const [error, setError] = useState('')
	const navigate = useNavigate()

	const handleLogin = () => {
		axios
			.post('http://localhost:5000/api/auth/login', { email, password }) // ✅ Fixed API path
			.then((response) => {
				if (response.data.error) {
					setError(response.data.error)
				} else {
					// ✅ Save user info in localStorage
					localStorage.setItem('user', JSON.stringify(response.data.user))

					// ✅ Redirect to Dashboard
					navigate('/dashboard')
				}
			})
			.catch(() => setError('Server error. Try again later.'))
	}

	return (
		<div className="flex flex-col items-center justify-center min-h-screen bg-gray-100">
			<h1 className="text-2xl font-bold mb-4">Sign In</h1>
			{error && <p className="text-red-500">{error}</p>}

			<input
				type="email"
				placeholder="Email"
				className="p-2 border rounded mb-2 w-80"
				value={email}
				onChange={(e) => setEmail(e.target.value)}
			/>

			<input
				type="password"
				placeholder="Password"
				className="p-2 border rounded mb-2 w-80"
				value={password}
				onChange={(e) => setPassword(e.target.value)}
			/>

			<button
				className="bg-primary text-white p-2 rounded w-80"
				onClick={handleLogin}
			>
				Sign In
			</button>

			<p className="mt-2">
				Don't have an account?{' '}
				<Link to="/signup" className="text-primary">
					Sign Up
				</Link>
			</p>
		</div>
	)
}

export default SignIn
