/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#2563EB', // Blue - trust & medical
        secondary: '#10B981', // Green - care & safety
        background: '#F9FAFB', // Light clean background
        accent: '#6366F1' // Soft indigo highlights
      }
    },
  },
  corePlugins: {
    preflight: false,
  },
  plugins: [],
}
