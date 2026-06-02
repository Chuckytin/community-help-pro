import { Routes, Route, Navigate } from "react-router-dom";
import Home from "./pages/Home";
import Login from "./pages/auth/Login";
import EmailVerify from "./pages/auth/EmailVerify";
import ResetPassword from "./pages/auth/ResetPassword";
import OAuth2Callback from "./pages/auth/OAuth2Callback";
import NewHelpRequest from "./pages/help-requests/NewHelpRequest";
import MyHelpRequests from "./pages/help-requests/MyHelpRequests";
import HelpRequestDetail from "./pages/help-requests/HelpRequestDetail";
import EditHelpRequest from "./pages/help-requests/EditHelpRequest";
import NewDonation from "./pages/donations/NewDonation";
import MyDonations from "./pages/donations/MyDonations";
import DonationDetail from "./pages/donations/DonationDetail";
import EditDonation from "./pages/donations/EditDonation";
import UserProfile from "./pages/profile/UserProfile";
import VolunteerProfilePage from "./pages/profile/VolunteerProfile";
import VolunteerAssigned from "./pages/volunteer/VolunteerAssigned";
import ConversationList from "./pages/chat/ConversationList";
import ChatPage from "./pages/chat/ChatPage";
import ReviewForm from "./pages/reviews/ReviewForm";

function App() {
    return (
        <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/email-verify" element={<EmailVerify />} />
            <Route path="/reset-password" element={<ResetPassword />} />
            <Route path="/oauth2/callback" element={<OAuth2Callback />} />

            <Route path="/help-requests/new" element={<NewHelpRequest />} />
            <Route path="/help-requests/me" element={<MyHelpRequests />} />
            <Route path="/help-requests/:id" element={<HelpRequestDetail />} />
            <Route path="/help-requests/:id/edit" element={<EditHelpRequest />} />

            <Route path="/donations/new" element={<NewDonation />} />
            <Route path="/donations/me" element={<MyDonations />} />
            <Route path="/donations/:id" element={<DonationDetail />} />
            <Route path="/donations/:id/edit" element={<EditDonation />} />

            <Route path="/profile" element={<UserProfile />} />
            <Route path="/profile/volunteer" element={<VolunteerProfilePage />} />
            <Route path="/volunteer/assigned" element={<VolunteerAssigned />} />

            <Route path="/chat" element={<ConversationList />} />
            <Route path="/chat/:id" element={<ChatPage />} />

            <Route path="/reviews/new" element={<ReviewForm />} />

            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    );
}

export default App;