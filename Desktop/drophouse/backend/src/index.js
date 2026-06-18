require('dotenv').config()
const express = require('express')
const cors = require('cors')
require('express-async-errors')

const userRoutes = require('./routes/users')

const app = express()

app.use(cors({
origin: process.env.FRONTEND_URL,
credentials: true
}))

app.use(express.json())

app.use('/api/users', userRoutes)

app.get('/', (req, res) => {
res.json({ message: 'DropHouse API is running' })
})

app.use((err, req, res, next) => {
console.error(err)
res.status(500).json({ message: err.message || 'Something went wrong' })
})

const PORT = process.env.PORT || 5000
app.listen(PORT, () => {
console.log(`Server running on port ${PORT}`)
})