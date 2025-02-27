require('dotenv').config()
const express = require('express')
const cors = require('cors')
const path = require('path')

const app = express()

// ✅ CORS Configuration
app.use(
	cors({
		origin: process.env.FRONTEND_URL || 'http://localhost:3000',
		credentials: true,
		methods: ['GET', 'POST', 'PUT', 'DELETE'],
		allowedHeaders: ['Content-Type', 'Authorization'],
	})
)

app.use(express.json())
app.use(express.urlencoded({ extended: true }))

// ✅ API Routes
app.use('/api/auth', require('./routes/authRoutes'))
app.use('/api/courses', require('./routes/courseRoutes'))
app.use('/api/study-groups', require('./routes/studyGroupRoutes'))
app.use('/api/chat', require('./routes/chatRoutes'))
app.use('/api/study-materials', require('./routes/fileRoutes'))

// ✅ File Serving
app.use('/uploads', express.static(path.join(__dirname, 'uploads')))

// ✅ Start Server
const PORT = process.env.PORT || 5000
app.listen(PORT, () => {
	console.log(`🚀 Server running on port ${PORT}`)
})
