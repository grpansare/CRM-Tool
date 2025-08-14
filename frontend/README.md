# CRM Platform Frontend

A modern React.js frontend for the CRM Platform, featuring tenant registration, user authentication, and a comprehensive dashboard.

## Features

- **Modern UI/UX**: Built with Tailwind CSS and Lucide React icons
- **Tenant Registration**: Complete organization signup flow
- **User Authentication**: JWT-based login/logout system
- **Responsive Design**: Mobile-first approach
- **Form Validation**: React Hook Form with comprehensive validation
- **Toast Notifications**: User feedback with react-hot-toast
- **Protected Routes**: Role-based access control
- **Dashboard**: Overview with stats and quick actions

## Tech Stack

- **React 18**: Latest React with hooks
- **React Router DOM**: Client-side routing
- **Tailwind CSS**: Utility-first CSS framework
- **React Hook Form**: Form handling and validation
- **Axios**: HTTP client for API calls
- **Lucide React**: Beautiful icons
- **React Hot Toast**: Toast notifications

## Getting Started

### Prerequisites

- Node.js 16+
- npm or yarn
- Backend API running on `http://localhost:8080`

### Installation

1. **Install dependencies**:

   ```bash
   cd frontend
   npm install
   ```

2. **Start the development server**:

   ```bash
   npm start
   ```

3. **Open your browser**:
   Navigate to `http://localhost:3000`

### Build for Production

```bash
npm run build
```

## Project Structure

```
frontend/
├── public/
│   └── index.html
├── src/
│   ├── components/          # Reusable components
│   │   ├── Contacts.js
│   │   ├── Deals.js
│   │   ├── Accounts.js
│   │   └── UsersManagement.js
│   ├── contexts/           # React contexts
│   │   └── AuthContext.js
│   ├── pages/              # Page components
│   │   ├── LandingPage.js
│   │   ├── TenantRegistration.js
│   │   ├── Login.js
│   │   └── Dashboard.js
│   ├── services/           # API services
│   │   └── api.js
│   ├── App.js              # Main app component
│   ├── index.js            # Entry point
│   └── index.css           # Global styles
├── package.json
├── tailwind.config.js
└── postcss.config.js
```

## Key Features

### 1. Landing Page

- Hero section with call-to-action
- Feature highlights
- Pricing plans
- Professional design

### 2. Tenant Registration

- Comprehensive organization signup form
- **Optional custom subdomain** (auto-generated if not provided)
- Real-time subdomain availability checking
- Form validation with error messages
- Subscription plan selection
- Terms and conditions acceptance

### 3. User Authentication

- Clean login form
- Password visibility toggle
- Remember me functionality
- Error handling and user feedback

### 4. Dashboard

- Overview with key metrics
- Navigation sidebar
- Quick actions
- Recent activity feed
- Role-based menu items

## API Integration

The frontend integrates with the backend API through:

- **Authentication**: JWT token management
- **Tenant Registration**: Organization creation
- **User Management**: Login/logout and user data
- **Protected Routes**: Role-based access control

## Styling

- **Tailwind CSS**: Utility-first styling
- **Custom Components**: Reusable styled components
- **Responsive Design**: Mobile-first approach
- **Dark Mode Ready**: Easy to extend for dark theme

## Development

### Available Scripts

- `npm start`: Start development server
- `npm build`: Build for production
- `npm test`: Run tests
- `npm eject`: Eject from Create React App

### Environment Variables

Create a `.env` file in the frontend directory:

```env
REACT_APP_API_URL=http://localhost:8080
```

## Contributing

1. Follow the existing code style
2. Use meaningful component and variable names
3. Add proper error handling
4. Test your changes thoroughly
5. Update documentation as needed

## Deployment

The frontend can be deployed to any static hosting service:

- **Netlify**: Drag and drop the `build` folder
- **Vercel**: Connect your GitHub repository
- **AWS S3**: Upload the `build` folder
- **Firebase Hosting**: Use Firebase CLI

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## License

This project is part of the CRM Platform and follows the same license terms.
