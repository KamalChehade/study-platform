import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { FiMenu, FiX, FiHome, FiBook, FiUsers, FiLogOut } from 'react-icons/fi'

function Layout({ children }) {
	const [isOpen, setIsOpen] = useState(false)
	const navigate = useNavigate()
	const user = JSON.parse(localStorage.getItem('user'))

	// ✅ Logout function
	const handleLogout = () => {
		localStorage.removeItem('user') // Remove user session
		navigate('/signin') // Redirect to login
	}

	return (
		<div className="container-fluid w-full">
			<div className="flex h-screen bg-gray-100">
				{/* Sidebar */}
				<div
					className={`fixed inset-y-0 left-0 z-50 transform ${
						isOpen ? 'translate-x-0' : '-translate-x-full'
					} transition-transform duration-300 ease-in-out bg-white w-64 shadow-lg md:relative md:translate-x-0`}
				>
					<div className="flex justify-between items-center mb-5  p-5">
						<h2 className="text-2xl font-bold text-primary">Dashboard</h2>
						<button className="md:hidden" onClick={() => setIsOpen(false)}>
							<FiX className="w-6 h-6" />
						</button>
					</div>

					<nav className="space-y-4">
						<button
							onClick={() => navigate('/dashboard')}
							className="flex items-center w-full text-gray-700 hover:bg-gray-200 p-5 duration-500"
						>
							<FiHome className="w-5 h-5 mr-2" /> Home
						</button>

						{user?.role === 'Student' && (
							<button
								onClick={() => navigate('/courses')}
								className="flex items-center w-full text-gray-700 hover:bg-gray-200 p-5 duration-500"
							>
								<FiBook className="w-5 h-5 mr-2" /> Courses
							</button>
						)}

						<button
							onClick={() => navigate('/study-groups')}
							className="flex items-center w-full text-gray-700 hover:bg-gray-200 p-5 duration-500"
						>
							<FiUsers className="w-5 h-5 mr-2" /> Study Groups
						</button>
					</nav>

					{/* ✅ Logout Button at the Bottom */}
					<button
						onClick={handleLogout}
						className="absolute bottom-5 flex items-center p-2 text-red-500 hover:bg-red-100  w-full duration-500"
					>
						<FiLogOut className="w-5 h-5 mr-2" /> Logout
					</button>
				</div>

				{/* Main Content */}
				<div className="flex-1 flex flex-col">
					{/* Top Navigation */}
					<div className="bg-white shadow p-5 flex items-center justify-between md:hidden">
						<button onClick={() => setIsOpen(true)}>
							<FiMenu className="w-6 h-6" />
						</button>
						<h1 className="text-lg font-bold text-primary">Dashboard</h1>
					</div>

					{/* Page Content */}
					<div className="p-6">{children}</div>
				</div>
			</div>
		</div>
	)
}

export default Layout
