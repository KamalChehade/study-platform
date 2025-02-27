import { useState } from 'react'
import axios from 'axios'

function FileUpload({ groupID, setMaterials }) {
	const [files, setFiles] = useState([])
	const user = JSON.parse(localStorage.getItem('user')) // ✅ Fetch user details

	const handleFileChange = (e) => {
		setFiles(e.target.files) // ✅ Capture selected files
	}

	const handleUpload = async () => {
		if (files.length === 0) {
			console.log('Please select at least one file.')
			return
		}

		// ✅ Ensure user is a teacher
		if (!user || user.role !== 'Teacher') {
			console.log('Only teachers can upload files.')
			return
		}

		const formData = new FormData()
		for (let i = 0; i < files.length; i++) {
			formData.append('files', files[i]) // ✅ Append files
		}
		formData.append('groupID', groupID) // ✅ Append groupID
		formData.append('teacherID', user.userID) // ✅ Append teacherID

		try {
			const res = await axios.post(
				'http://localhost:5000/api/study-materials/upload',
				formData,
				{
					headers: {
						'Content-Type': 'multipart/form-data', // ✅ Correct header for FormData
					},
				}
			)

			// ✅ Update materials list in UI
			setMaterials((prev) => [...prev, ...res.data])
			setFiles([]) // ✅ Reset file selection
			console.log('Files uploaded successfully!')
		} catch (error) {
			console.error('❌ Upload Failed:', error)
			console.log('Failed to upload files.')
		}
	}

	return (
		<div className="mt-4 p-4 bg-white shadow rounded">
			<input
				type="file"
				multiple
				onChange={handleFileChange}
				className="border p-2 w-full"
			/>
			<button
				onClick={handleUpload}
				className="mt-2 bg-blue-500 text-white px-4 py-2 rounded"
			>
				Upload
			</button>
		</div>
	)
}

export default FileUpload
