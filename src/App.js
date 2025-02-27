import { Routes, Route, Navigate } from 'react-router-dom'
import SignIn from './pages/SignIn'
import SignUp from './pages/SignUp'
import Dashboard from './pages/Dashboard'
import Courses from './pages/Courses'
import StudyGroups from './pages/StudyGroups'
import StudyGroupDetails from './pages/StudyGroupDetails'

// ✅ Auth Check Function
const ProtectedRoute = ({ element }) => {
	const user = JSON.parse(localStorage.getItem('user'))
	return user ? element : <Navigate to="/signin" />
}

function App() {
	return (
		<div className="flex items-center justify-center min-h-screen bg-gray-100">
			<Routes>
				{/* ✅ Redirect root to Sign In */}
				<Route path="/" element={<Navigate to="/signin" />} />

				{/* ✅ Public Routes */}
				<Route path="/signin" element={<SignIn />} />
				<Route path="/signup" element={<SignUp />} />

				{/* ✅ Protected Routes (Only accessible if logged in) */}
				<Route
					path="/dashboard"
					element={<ProtectedRoute element={<Dashboard />} />}
				/>
				<Route
					path="/courses"
					element={<ProtectedRoute element={<Courses />} />}
				/>
				<Route
					path="/study-groups"
					element={<ProtectedRoute element={<StudyGroups />} />}
				/>
				<Route
					path="/study-groups/:groupID"
					element={<ProtectedRoute element={<StudyGroupDetails />} />}
				/>
			</Routes>
		</div>
	)
}

export default App
