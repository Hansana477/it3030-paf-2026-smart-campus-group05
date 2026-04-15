import React, { useState, useEffect } from 'react';
import {
  Calendar,
  Clock,
  Users,
  MapPin,
  CheckCircle,
  XCircle,
  AlertCircle,
  Search,
  Filter,
  ChevronLeft,
  ChevronRight,
  Eye,
  Download,
  Printer,
  RefreshCw,
  ChevronDown,
  Loader2,
  X,
  Save,
  Phone,
  Mail,
  User,
  BookOpen,
  Building,
  Armchair,
  Zap,
  Wifi,
  FileText,
  TrendingUp,
  BarChart3,
  PieChart,
} from 'lucide-react';

// ==============================================
// DUMMY BOOKING DATA
// ==============================================
const DUMMY_BOOKINGS = [
  {
    id: 'BK001',
    resourceId: '1',
    resourceName: 'Main Lecture Hall A',
    resourceType: 'LECTURE_HALL',
    location: 'Building A, Floor 1',
    capacity: 120,
    bookedSeats: 85,
    requesterName: 'John Doe',
    requesterId: 'STU001',
    requesterEmail: 'john.doe@university.edu',
    requesterPhone: '+94-701-234-567',
    date: '2024-12-15',
    startTime: '09:00',
    endTime: '11:00',
    purpose: 'Advanced Programming Lecture',
    expectedAttendees: 85,
    createdDate: '2024-12-08',
    requestedDate: '2024-12-08',
    status: 'PENDING',
    notes: 'Need projector and sound system working',
    rejectionReason: null,
    approverName: null,
    amenitiesRequired: ['Projector', 'Sound System', 'WiFi'],
  },
  {
    id: 'BK002',
    resourceId: '2',
    resourceName: 'Computer Lab 301',
    resourceType: 'LAB',
    location: 'Building C, Floor 3',
    capacity: 30,
    bookedSeats: 25,
    requesterName: 'Sarah Smith',
    requesterId: 'STU002',
    requesterEmail: 'sarah.smith@university.edu',
    requesterPhone: '+94-701-234-568',
    date: '2024-12-16',
    startTime: '13:00',
    endTime: '15:00',
    purpose: 'Database Design Practical Session',
    expectedAttendees: 25,
    createdDate: '2024-12-08',
    requestedDate: '2024-12-08',
    status: 'PENDING',
    notes: 'Students need to install PostgreSQL beforehand',
    rejectionReason: null,
    approverName: null,
    amenitiesRequired: ['Computers', 'Software Licenses'],
  },
  {
    id: 'BK003',
    resourceId: '3',
    resourceName: 'Conference Room B',
    resourceType: 'MEETING_ROOM',
    location: 'Building B, Floor 2',
    capacity: 12,
    bookedSeats: 8,
    requesterName: 'Michael Johnson',
    requesterId: 'STU003',
    requesterEmail: 'michael.j@university.edu',
    requesterPhone: '+94-701-234-569',
    date: '2024-12-17',
    startTime: '10:00',
    endTime: '11:30',
    purpose: 'Research Team Meeting',
    expectedAttendees: 8,
    createdDate: '2024-12-07',
    requestedDate: '2024-12-07',
    status: 'APPROVED',
    notes: 'Need video conferencing setup',
    rejectionReason: null,
    approverName: 'Admin User',
    amenitiesRequired: ['Video Conference', 'Smart Board'],
  },
  {
    id: 'BK004',
    resourceId: '1',
    resourceName: 'Main Lecture Hall A',
    resourceType: 'LECTURE_HALL',
    location: 'Building A, Floor 1',
    capacity: 120,
    bookedSeats: 100,
    requesterName: 'Emma Wilson',
    requesterId: 'STU004',
    requesterEmail: 'emma.w@university.edu',
    requesterPhone: '+94-701-234-570',
    date: '2024-12-18',
    startTime: '14:00',
    endTime: '16:00',
    purpose: 'Annual Student Summit',
    expectedAttendees: 100,
    createdDate: '2024-12-06',
    requestedDate: '2024-12-06',
    status: 'REJECTED',
    notes: 'Time conflict with faculty meeting',
    rejectionReason: 'Time slot already booked by faculty',
    approverName: 'Admin User',
    amenitiesRequired: ['Projector', 'Sound System', 'WiFi', 'Whiteboard'],
  },
  {
    id: 'BK005',
    resourceId: '5',
    resourceName: 'Silent Study Area',
    resourceType: 'STUDY_AREA',
    location: 'Library, Floor 2',
    capacity: 50,
    bookedSeats: 40,
    requesterName: 'Alex Kumar',
    requesterId: 'STU005',
    requesterEmail: 'alex.k@university.edu',
    requesterPhone: '+94-701-234-571',
    date: '2024-12-19',
    startTime: '15:00',
    endTime: '18:00',
    purpose: 'Final Exam Group Study',
    expectedAttendees: 40,
    createdDate: '2024-12-05',
    requestedDate: '2024-12-05',
    status: 'APPROVED',
    notes: 'Please ensure all seating areas are available',
    rejectionReason: null,
    approverName: 'Admin User',
    amenitiesRequired: ['WiFi', 'Power Outlets'],
  },
  {
    id: 'BK006',
    resourceId: '4',
    resourceName: 'Portable Projector',
    resourceType: 'EQUIPMENT',
    location: 'AV Room, Building A',
    capacity: 1,
    bookedSeats: 1,
    requesterName: 'Lisa Chen',
    requesterId: 'STU006',
    requesterEmail: 'lisa.chen@university.edu',
    requesterPhone: '+94-701-234-572',
    date: '2024-12-20',
    startTime: '11:00',
    endTime: '13:00',
    purpose: 'Multimedia Project Presentation',
    expectedAttendees: 50,
    createdDate: '2024-12-04',
    requestedDate: '2024-12-04',
    status: 'PENDING',
    notes: 'Need HDMI and VGA cables included',
    rejectionReason: null,
    approverName: null,
    amenitiesRequired: ['HDMI Cable', 'VGA Cable'],
  },
  {
    id: 'BK007',
    resourceId: '2',
    resourceName: 'Computer Lab 301',
    resourceType: 'LAB',
    location: 'Building C, Floor 3',
    capacity: 30,
    bookedSeats: 20,
    requesterName: 'David Brown',
    requesterId: 'STU007',
    requesterEmail: 'david.b@university.edu',
    requesterPhone: '+94-701-234-573',
    date: '2024-12-21',
    startTime: '09:00',
    endTime: '12:00',
    purpose: 'Web Development Workshop',
    expectedAttendees: 20,
    createdDate: '2024-12-03',
    requestedDate: '2024-12-03',
    status: 'CANCELLED',
    notes: 'Instructor cancelled due to illness',
    rejectionReason: 'Cancelled by requester',
    approverName: 'Admin User',
    amenitiesRequired: ['Computers', 'Software Licenses', 'Printers'],
  },
  {
    id: 'BK008',
    resourceId: '3',
    resourceName: 'Conference Room B',
    resourceType: 'MEETING_ROOM',
    location: 'Building B, Floor 2',
    capacity: 12,
    bookedSeats: 6,
    requesterName: 'Rachel Green',
    requesterId: 'STU008',
    requesterEmail: 'rachel.g@university.edu',
    requesterPhone: '+94-701-234-574',
    date: '2024-12-22',
    startTime: '16:00',
    endTime: '17:00',
    purpose: 'Department Committee Meeting',
    expectedAttendees: 6,
    createdDate: '2024-12-02',
    requestedDate: '2024-12-02',
    status: 'PENDING',
    notes: 'Regular monthly meeting',
    rejectionReason: null,
    approverName: null,
    amenitiesRequired: ['Video Conference'],
  },
];

// Status badge color mapping
const STATUS_COLORS = {
  PENDING: { bg: 'bg-yellow-50', border: 'border-yellow-200', text: 'text-yellow-800', icon: 'text-yellow-600' },
  APPROVED: { bg: 'bg-green-50', border: 'border-green-200', text: 'text-green-800', icon: 'text-green-600' },
  REJECTED: { bg: 'bg-red-50', border: 'border-red-200', text: 'text-red-800', icon: 'text-red-600' },
  CANCELLED: { bg: 'bg-gray-50', border: 'border-gray-200', text: 'text-gray-800', icon: 'text-gray-600' },
};

// ==============================================
// MAIN COMPONENT
// ==============================================
const AdminBookingManagement = () => {
  const [bookings, setBookings] = useState(DUMMY_BOOKINGS);
  const [selectedBooking, setSelectedBooking] = useState(null);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [showActionModal, setShowActionModal] = useState(false);
  const [actionType, setActionType] = useState('approve'); // 'approve' or 'reject'
  const [rejectionReason, setRejectionReason] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('ALL');
  const [selectedResourceType, setSelectedResourceType] = useState('ALL');
  const [sortBy, setSortBy] = useState('date');
  const [showNotification, setShowNotification] = useState(false);
  const [notificationMessage, setNotificationMessage] = useState('');
  const [notificationType, setNotificationType] = useState('success');
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 5;

  // Show notification
  const showNotificationMessage = (message, type = 'success') => {
    setNotificationMessage(message);
    setNotificationType(type);
    setShowNotification(true);
    setTimeout(() => setShowNotification(false), 3000);
  };

  // Filter bookings
  const filteredBookings = bookings.filter(booking => {
    const matchesSearch = 
      booking.resourceName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      booking.requesterName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      booking.id.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = selectedStatus === 'ALL' || booking.status === selectedStatus;
    const matchesType = selectedResourceType === 'ALL' || booking.resourceType === selectedResourceType;
    return matchesSearch && matchesStatus && matchesType;
  });

  // Sort bookings
  const sortedBookings = [...filteredBookings].sort((a, b) => {
    switch (sortBy) {
      case 'date':
        return new Date(b.date) - new Date(a.date);
      case 'status':
        return a.status.localeCompare(b.status);
      case 'attendees':
        return b.expectedAttendees - a.expectedAttendees;
      default:
        return 0;
    }
  });

  // Pagination
  const totalPages = Math.ceil(sortedBookings.length / itemsPerPage);
  const startIdx = (currentPage - 1) * itemsPerPage;
  const paginatedBookings = sortedBookings.slice(startIdx, startIdx + itemsPerPage);

  // Handle approval/rejection
  const handleApproveBooking = () => {
    if (!selectedBooking) return;
    setLoading(true);
    setTimeout(() => {
      setBookings(prev => prev.map(b => 
        b.id === selectedBooking.id 
          ? { ...b, status: 'APPROVED', approverName: 'Admin User' }
          : b
      ));
      showNotificationMessage('Booking approved successfully!', 'success');
      setShowActionModal(false);
      setShowDetailModal(false);
      setLoading(false);
    }, 500);
  };

  const handleRejectBooking = () => {
    if (!selectedBooking || !rejectionReason.trim()) {
      showNotificationMessage('Please provide a rejection reason', 'error');
      return;
    }
    setLoading(true);
    setTimeout(() => {
      setBookings(prev => prev.map(b => 
        b.id === selectedBooking.id 
          ? { ...b, status: 'REJECTED', rejectionReason, approverName: 'Admin User' }
          : b
      ));
      showNotificationMessage('Booking rejected successfully!', 'success');
      setRejectionReason('');
      setShowActionModal(false);
      setShowDetailModal(false);
      setLoading(false);
    }, 500);
  };

  const openActionModal = (booking, type) => {
    setSelectedBooking(booking);
    setActionType(type);
    setRejectionReason('');
    setShowActionModal(true);
  };

  // Calculate statistics
  const stats = {
    total: bookings.length,
    pending: bookings.filter(b => b.status === 'PENDING').length,
    approved: bookings.filter(b => b.status === 'APPROVED').length,
    rejected: bookings.filter(b => b.status === 'REJECTED').length,
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-cyan-50 to-emerald-50 p-6">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-4xl font-bold text-slate-900 mb-2">Booking Management</h1>
        <p className="text-slate-600">Review, approve, or reject resource booking requests</p>
      </div>

      {/* Statistics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
        <StatCard label="Total Requests" value={stats.total} icon={<BookOpen />} color="blue" />
        <StatCard label="Pending" value={stats.pending} icon={<AlertCircle />} color="yellow" />
        <StatCard label="Approved" value={stats.approved} icon={<CheckCircle />} color="green" />
        <StatCard label="Rejected" value={stats.rejected} icon={<XCircle />} color="red" />
      </div>

      {/* Filters and Search */}
      <div className="bg-white rounded-lg shadow-sm border border-slate-200 p-6 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {/* Search */}
          <div className="relative">
            <Search className="absolute left-3 top-3 w-5 h-5 text-slate-400" />
            <input
              type="text"
              placeholder="Search booking ID, resource, or requester..."
              value={searchTerm}
              onChange={(e) => {
                setSearchTerm(e.target.value);
                setCurrentPage(1);
              }}
              className="w-full pl-10 pr-4 py-2 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-500"
            />
          </div>

          {/* Status Filter */}
          <select
            value={selectedStatus}
            onChange={(e) => {
              setSelectedStatus(e.target.value);
              setCurrentPage(1);
            }}
            className="px-4 py-2 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-500"
          >
            <option value="ALL">All Status</option>
            <option value="PENDING">Pending</option>
            <option value="APPROVED">Approved</option>
            <option value="REJECTED">Rejected</option>
            <option value="CANCELLED">Cancelled</option>
          </select>

          {/* Resource Type Filter */}
          <select
            value={selectedResourceType}
            onChange={(e) => {
              setSelectedResourceType(e.target.value);
              setCurrentPage(1);
            }}
            className="px-4 py-2 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-500"
          >
            <option value="ALL">All Resources</option>
            <option value="LECTURE_HALL">Lecture Hall</option>
            <option value="LAB">Lab</option>
            <option value="MEETING_ROOM">Meeting Room</option>
            <option value="STUDY_AREA">Study Area</option>
            <option value="EQUIPMENT">Equipment</option>
          </select>

          {/* Sort */}
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            className="px-4 py-2 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-cyan-500"
          >
            <option value="date">Sort by Date</option>
            <option value="status">Sort by Status</option>
            <option value="attendees">Sort by Attendees</option>
          </select>
        </div>
      </div>

      {/* Bookings Table */}
      <div className="bg-white rounded-lg shadow-sm border border-slate-200 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gradient-to-r from-cyan-50 to-blue-50 border-b border-slate-200">
              <tr>
                <th className="px-6 py-3 text-left text-sm font-semibold text-slate-700">Booking ID</th>
                <th className="px-6 py-3 text-left text-sm font-semibold text-slate-700">Resource</th>
                <th className="px-6 py-3 text-left text-sm font-semibold text-slate-700">Requester</th>
                <th className="px-6 py-3 text-left text-sm font-semibold text-slate-700">Date & Time</th>
                <th className="px-6 py-3 text-left text-sm font-semibold text-slate-700">Attendees</th>
                <th className="px-6 py-3 text-left text-sm font-semibold text-slate-700">Status</th>
                <th className="px-6 py-3 text-left text-sm font-semibold text-slate-700">Actions</th>
              </tr>
            </thead>
            <tbody>
              {paginatedBookings.length > 0 ? (
                paginatedBookings.map((booking) => (
                  <tr key={booking.id} className="border-b border-slate-200 hover:bg-slate-50 transition-colors">
                    <td className="px-6 py-4">
                      <span className="font-medium text-slate-900">{booking.id}</span>
                    </td>
                    <td className="px-6 py-4">
                      <div>
                        <p className="font-medium text-slate-900 text-sm">{booking.resourceName}</p>
                        <p className="text-xs text-slate-500">{booking.location}</p>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="text-sm">
                        <p className="font-medium text-slate-900">{booking.requesterName}</p>
                        <p className="text-xs text-slate-500">{booking.requesterEmail}</p>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="text-sm">
                        <p className="font-medium text-slate-900">{booking.date}</p>
                        <p className="text-xs text-slate-500">{booking.startTime} - {booking.endTime}</p>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="text-sm">
                        <p className="font-medium text-slate-900">{booking.expectedAttendees}/{booking.capacity}</p>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <StatusBadge status={booking.status} />
                    </td>
                    <td className="px-6 py-4">
                      <button
                        onClick={() => {
                          setSelectedBooking(booking);
                          setShowDetailModal(true);
                        }}
                        className="inline-flex items-center gap-2 px-3 py-1 bg-cyan-100 text-cyan-700 rounded-lg hover:bg-cyan-200 transition-colors text-sm font-medium"
                      >
                        <Eye className="w-4 h-4" />
                        View
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="7" className="px-6 py-8 text-center text-slate-500">
                    No bookings found matching your criteria
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex items-center justify-between px-6 py-4 bg-slate-50 border-t border-slate-200">
            <p className="text-sm text-slate-600">
              Showing {startIdx + 1} to {Math.min(startIdx + itemsPerPage, sortedBookings.length)} of {sortedBookings.length} bookings
            </p>
            <div className="flex gap-2">
              <button
                onClick={() => setCurrentPage(prev => Math.max(1, prev - 1))}
                disabled={currentPage === 1}
                className="p-2 border border-slate-200 rounded-lg hover:bg-white disabled:opacity-50"
              >
                <ChevronLeft className="w-4 h-4" />
              </button>
              {Array.from({ length: totalPages }, (_, i) => i + 1).map(page => (
                <button
                  key={page}
                  onClick={() => setCurrentPage(page)}
                  className={`px-3 py-1 rounded-lg font-medium transition-colors ${
                    currentPage === page
                      ? 'bg-cyan-500 text-white'
                      : 'border border-slate-200 hover:bg-slate-100'
                  }`}
                >
                  {page}
                </button>
              ))}
              <button
                onClick={() => setCurrentPage(prev => Math.min(totalPages, prev + 1))}
                disabled={currentPage === totalPages}
                className="p-2 border border-slate-200 rounded-lg hover:bg-white disabled:opacity-50"
              >
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Notification */}
      {showNotification && (
        <div className={`fixed top-4 right-4 p-4 rounded-lg shadow-lg text-white font-medium ${
          notificationType === 'success' ? 'bg-green-500' : 'bg-red-500'
        }`}>
          {notificationMessage}
        </div>
      )}

      {/* Detail Modal */}
      {showDetailModal && selectedBooking && (
        <BookingDetailModal
          booking={selectedBooking}
          onClose={() => setShowDetailModal(false)}
          onApprove={() => openActionModal(selectedBooking, 'approve')}
          onReject={() => openActionModal(selectedBooking, 'reject')}
          canApproveReject={selectedBooking.status === 'PENDING'}
        />
      )}

      {/* Action Modal */}
      {showActionModal && selectedBooking && (
        <ActionModal
          booking={selectedBooking}
          action={actionType}
          rejectionReason={rejectionReason}
          onReasonChange={setRejectionReason}
          onConfirm={actionType === 'approve' ? handleApproveBooking : handleRejectBooking}
          onCancel={() => setShowActionModal(false)}
          loading={loading}
        />
      )}
    </div>
  );
};

// ==============================================
// HELPER COMPONENTS
// ==============================================

const StatCard = ({ label, value, icon, color }) => {
  const colorClasses = {
    blue: 'bg-gradient-to-br from-blue-50 to-cyan-50 border-blue-200 text-blue-700',
    yellow: 'bg-gradient-to-br from-yellow-50 to-amber-50 border-yellow-200 text-yellow-700',
    green: 'bg-gradient-to-br from-green-50 to-emerald-50 border-green-200 text-green-700',
    red: 'bg-gradient-to-br from-red-50 to-pink-50 border-red-200 text-red-700',
  };

  return (
    <div className={`${colorClasses[color]} border rounded-lg p-6`}>
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-medium opacity-75">{label}</p>
          <p className="text-3xl font-bold mt-2">{value}</p>
        </div>
        <div className="opacity-20">{icon}</div>
      </div>
    </div>
  );
};

const StatusBadge = ({ status }) => {
  const colors = STATUS_COLORS[status] || STATUS_COLORS.PENDING;
  const statusLabels = {
    PENDING: 'Pending Review',
    APPROVED: 'Approved',
    REJECTED: 'Rejected',
    CANCELLED: 'Cancelled',
  };

  const statusIcons = {
    PENDING: <AlertCircle className="w-4 h-4" />,
    APPROVED: <CheckCircle className="w-4 h-4" />,
    REJECTED: <XCircle className="w-4 h-4" />,
    CANCELLED: <AlertCircle className="w-4 h-4" />,
  };

  return (
    <div className={`${colors.bg} ${colors.border} border rounded-full px-3 py-1 flex items-center gap-2 w-fit`}>
      <span className={colors.icon}>{statusIcons[status]}</span>
      <span className={`${colors.text} text-xs font-semibold`}>{statusLabels[status]}</span>
    </div>
  );
};

const BookingDetailModal = ({ booking, onClose, onApprove, onReject, canApproveReject }) => {
  const colors = STATUS_COLORS[booking.status];

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        {/* Modal Header */}
        <div className="flex items-center justify-between p-6 border-b border-slate-200">
          <div>
            <h2 className="text-2xl font-bold text-slate-900">Booking Details</h2>
            <p className="text-slate-600 text-sm mt-1">Booking ID: {booking.id}</p>
          </div>
          <button
            onClick={onClose}
            className="p-2 hover:bg-slate-100 rounded-lg transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Modal Content */}
        <div className="p-6 space-y-6">
          {/* Status Section */}
          <div className={`${colors.bg} ${colors.border} border rounded-lg p-4`}>
            <div className="flex items-center gap-3">
              <span className={colors.icon}>{STATUS_COLORS[booking.status].icon === 'text-yellow-600' ? <AlertCircle className="w-6 h-6" /> : <CheckCircle className="w-6 h-6" />}</span>
              <div>
                <p className={`${colors.text} font-semibold`}>
                  {booking.status === 'PENDING' ? 'Awaiting Approval' : booking.status}
                </p>
                {booking.rejectionReason && (
                  <p className={`${colors.text} text-sm`}>Reason: {booking.rejectionReason}</p>
                )}
              </div>
            </div>
          </div>

          {/* Resource Information */}
          <div className="grid grid-cols-2 gap-4">
            <div className="bg-slate-50 rounded-lg p-4">
              <label className="text-xs font-semibold text-slate-600 uppercase">Resource</label>
              <p className="text-lg font-bold text-slate-900 mt-1">{booking.resourceName}</p>
              <p className="text-sm text-slate-600 mt-1">{booking.location}</p>
              <p className="text-xs text-slate-500 mt-2">{booking.resourceType}</p>
            </div>
            <div className="bg-slate-50 rounded-lg p-4">
              <label className="text-xs font-semibold text-slate-600 uppercase">Capacity</label>
              <p className="text-lg font-bold text-slate-900 mt-1">{booking.expectedAttendees}/{booking.capacity}</p>
              <div className="w-full bg-slate-200 rounded-full h-2 mt-2">
                <div
                  className="bg-gradient-to-r from-cyan-500 to-blue-500 h-2 rounded-full"
                  style={{ width: `${(booking.expectedAttendees / booking.capacity) * 100}%` }}
                />
              </div>
            </div>
          </div>

          {/* Booking Details */}
          <div className="border border-slate-200 rounded-lg p-4">
            <h3 className="font-semibold text-slate-900 mb-4">Booking Information</h3>
            <div className="grid grid-cols-2 gap-4">
              <div className="flex items-center gap-3">
                <Calendar className="w-5 h-5 text-cyan-600" />
                <div>
                  <p className="text-xs text-slate-600">Date</p>
                  <p className="font-semibold text-slate-900">{booking.date}</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <Clock className="w-5 h-5 text-cyan-600" />
                <div>
                  <p className="text-xs text-slate-600">Time Slot</p>
                  <p className="font-semibold text-slate-900">{booking.startTime} - {booking.endTime}</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <FileText className="w-5 h-5 text-cyan-600" />
                <div>
                  <p className="text-xs text-slate-600">Purpose</p>
                  <p className="font-semibold text-slate-900">{booking.purpose}</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <Users className="w-5 h-5 text-cyan-600" />
                <div>
                  <p className="text-xs text-slate-600">Attendees</p>
                  <p className="font-semibold text-slate-900">{booking.expectedAttendees}</p>
                </div>
              </div>
            </div>
          </div>

          {/* Requester Details */}
          <div className="border border-slate-200 rounded-lg p-4">
            <h3 className="font-semibold text-slate-900 mb-4">Requester Information</h3>
            <div className="space-y-3">
              <div className="flex items-center gap-3">
                <User className="w-5 h-5 text-cyan-600" />
                <div>
                  <p className="text-xs text-slate-600">Name</p>
                  <p className="font-semibold text-slate-900">{booking.requesterName}</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <Mail className="w-5 h-5 text-cyan-600" />
                <div>
                  <p className="text-xs text-slate-600">Email</p>
                  <p className="font-semibold text-slate-900">{booking.requesterEmail}</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <Phone className="w-5 h-5 text-cyan-600" />
                <div>
                  <p className="text-xs text-slate-600">Phone</p>
                  <p className="font-semibold text-slate-900">{booking.requesterPhone}</p>
                </div>
              </div>
            </div>
          </div>

          {/* Amenities */}
          {booking.amenitiesRequired && booking.amenitiesRequired.length > 0 && (
            <div className="border border-slate-200 rounded-lg p-4">
              <h3 className="font-semibold text-slate-900 mb-3">Required Amenities</h3>
              <div className="flex flex-wrap gap-2">
                {booking.amenitiesRequired.map((amenity, idx) => (
                  <span
                    key={idx}
                    className="px-3 py-1 bg-cyan-100 text-cyan-700 rounded-full text-sm font-medium"
                  >
                    {amenity}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* Notes */}
          {booking.notes && (
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
              <h3 className="font-semibold text-blue-900 mb-2">Additional Notes</h3>
              <p className="text-blue-800 text-sm">{booking.notes}</p>
            </div>
          )}

          {/* Dates */}
          <div className="grid grid-cols-2 gap-4 text-sm text-slate-600">
            <div>
              <p className="text-xs font-semibold uppercase">Requested On</p>
              <p className="mt-1">{booking.requestedDate}</p>
            </div>
            {booking.approverName && (
              <div>
                <p className="text-xs font-semibold uppercase">Reviewed By</p>
                <p className="mt-1">{booking.approverName}</p>
              </div>
            )}
          </div>
        </div>

        {/* Modal Actions */}
        <div className="flex gap-3 p-6 border-t border-slate-200 bg-slate-50">
          {canApproveReject ? (
            <>
              <button
                onClick={onReject}
                className="flex-1 px-4 py-2 bg-red-100 text-red-700 rounded-lg hover:bg-red-200 font-medium transition-colors flex items-center justify-center gap-2"
              >
                <XCircle className="w-4 h-4" />
                Reject
              </button>
              <button
                onClick={onApprove}
                className="flex-1 px-4 py-2 bg-green-100 text-green-700 rounded-lg hover:bg-green-200 font-medium transition-colors flex items-center justify-center gap-2"
              >
                <CheckCircle className="w-4 h-4" />
                Approve
              </button>
            </>
          ) : (
            <button
              onClick={onClose}
              className="flex-1 px-4 py-2 bg-slate-200 text-slate-700 rounded-lg hover:bg-slate-300 font-medium transition-colors"
            >
              Close
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

const ActionModal = ({
  booking,
  action,
  rejectionReason,
  onReasonChange,
  onConfirm,
  onCancel,
  loading,
}) => {
  const isApprove = action === 'approve';

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-md w-full">
        {/* Header */}
        <div className={`p-6 border-b ${isApprove ? 'border-green-200 bg-green-50' : 'border-red-200 bg-red-50'}`}>
          <h2 className={`text-xl font-bold ${isApprove ? 'text-green-900' : 'text-red-900'}`}>
            {isApprove ? '✓ Approve Booking?' : '✗ Reject Booking?'}
          </h2>
          <p className={`text-sm mt-1 ${isApprove ? 'text-green-700' : 'text-red-700'}`}>
            Booking ID: {booking.id}
          </p>
        </div>

        {/* Content */}
        <div className="p-6 space-y-4">
          <div className="bg-slate-50 rounded-lg p-4">
            <p className="text-sm text-slate-600">
              <strong>{booking.resourceName}</strong>
            </p>
            <p className="text-xs text-slate-500 mt-1">
              {booking.date} • {booking.startTime} - {booking.endTime}
            </p>
          </div>

          {!isApprove && (
            <>
              <label className="block text-sm font-semibold text-slate-900">
                Rejection Reason *
              </label>
              <textarea
                value={rejectionReason}
                onChange={(e) => onReasonChange(e.target.value)}
                placeholder="Explain why this booking is being rejected..."
                className="w-full px-4 py-2 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500 text-sm"
                rows="4"
              />
            </>
          )}

          {isApprove && (
            <div className="bg-green-50 border border-green-200 rounded-lg p-4">
              <p className="text-sm text-green-800">
                The requester will receive an approval notification and the booking will be confirmed.
              </p>
            </div>
          )}
        </div>

        {/* Actions */}
        <div className="flex gap-3 p-6 border-t border-slate-200 bg-slate-50">
          <button
            onClick={onCancel}
            disabled={loading}
            className="flex-1 px-4 py-2 bg-slate-200 text-slate-700 rounded-lg hover:bg-slate-300 font-medium transition-colors disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            onClick={onConfirm}
            disabled={loading || (!isApprove && !rejectionReason.trim())}
            className={`flex-1 px-4 py-2 text-white rounded-lg font-medium transition-colors flex items-center justify-center gap-2 disabled:opacity-50 ${
              isApprove
                ? 'bg-green-500 hover:bg-green-600'
                : 'bg-red-500 hover:bg-red-600'
            }`}
          >
            {loading && <Loader2 className="w-4 h-4 animate-spin" />}
            {isApprove ? 'Approve' : 'Reject'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default AdminBookingManagement;
