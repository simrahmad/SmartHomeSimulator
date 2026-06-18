const express = require('express')
const router = express.Router()
const prisma = require('../middleware/prismaClient')


router.post('/sync', async (req, res) => {
	const { clerkId, firstName, lastName, email, profileImage } = req.body

	try {
		if (!clerkId || !email) {
			return res.status(400).json({ message: 'clerkId and email are required' })
		}

		// Check if user already exists
		const existing = await prisma.user.findUnique({
			where: { clerkId }
		})

		if (existing) {
			return res.json({ message: 'User already synced', user: existing })
		}

		// Use the exact enum value defined in the Prisma schema (case-sensitive)
		const user = await prisma.user.create({
			data: {
				clerkId,
				firstName: firstName || 'User',
				lastName: lastName || '',
				email,
				profileImage: profileImage || '',
				role: 'Customer'
			}
		})

		res.status(201).json({ message: 'User created', user })
	} catch (err) {
		console.error('Error in /users/sync:', err)
		return res.status(500).json({ message: 'Internal server error', error: err.message })
	}
})

router.get('/me/:clerkId', async (req, res) => {
const user = await prisma.user.findUnique({
where: { clerkId: req.params.clerkId }
})

if (!user) return res.status(404).json({ message: 'User not found' })

res.json(user)
})

module.exports = router