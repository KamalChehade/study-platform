import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Link } from 'react-router-dom'
import axios from 'axios'

function SignUp() {
	const [name, setName] = useState('')
	const [email, setEmail] = useState('')
	const [password, setPassword] = useState('')
	const [role, setRole] = useState('Student') // Default role is "Student"
	const [universityID, setUniversityID] = useState('')
	const [error, setError] = useState('')
	const navigate = useNavigate()

	const handleRegister = () => {
		axios
			.post('http://localhost:5000/api/auth/register', {
				name,
				email,
				password,
				role,
				universityID,
			})
			.then((response) => {
				console.log(response.data.message) // Show success message
				if (!response.data.error) {
					navigate('/signin') // Redirect to sign-in page
				}
			})
			.catch(() => setError('Server error. Try again later.'))
	}

	return (
		<div className="flex flex-col items-center justify-center min-h-screen bg-gray-100">
			<h1 className="text-2xl font-bold mb-4">Sign Up</h1>
			{error && <p className="text-red-500">{error}</p>}

			<input
				type="text"
				placeholder="Full Name"
				className="p-2 border rounded mb-2 w-80"
				value={name}
				onChange={(e) => setName(e.target.value)}
			/>

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

			<input
				type="text"
				placeholder="University ID"
				className="p-2 border rounded mb-2 w-80"
				value={universityID}
				onChange={(e) => setUniversityID(e.target.value)}
			/>

			{/* Role Selection Dropdown */}
			<select
				className="p-2 border rounded mb-2 w-80"
				value={role}
				onChange={(e) => setRole(e.target.value)}
			>
				<option value="Student">Student</option>
				<option value="Teacher">Teacher</option>
			</select>

			<button
				className="bg-primary text-white p-2 rounded w-80"
				onClick={handleRegister}
			>
				Sign Up
			</button>

			{/* Keep Login Message */}
			<p className="mt-2">
				Already have an account?{' '}
				<Link to="/signin" className="text-primary">
					Sign In
				</Link>
			</p>
		</div>
	)
}

export default SignUp
