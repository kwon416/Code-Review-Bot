// API Base URL
const API_BASE_URL = window.location.origin;

// Chart instances
let severityChart = null;
let categoryChart = null;

// Initialize dashboard
document.addEventListener('DOMContentLoaded', () => {
    loadDashboardData();
    loadRecentReviews();

    // Refresh every 30 seconds
    setInterval(() => {
        loadDashboardData();
        loadRecentReviews();
    }, 30000);
});

// Load dashboard statistics
async function loadDashboardData() {
    try {
        const response = await fetch(`${API_BASE_URL}/api/dashboard/statistics`);
        if (!response.ok) throw new Error('Failed to fetch statistics');

        const data = await response.json();
        updateStatistics(data);
        updateCharts(data);
    } catch (error) {
        console.error('Error loading dashboard data:', error);
        showError('통계 데이터를 불러오는데 실패했습니다.');
    }
}

// Update statistics
function updateStatistics(data) {
    const { overallStats, recentActivity } = data;

    // Overall stats
    document.getElementById('totalRepositories').textContent =
        overallStats.totalRepositories || 0;
    document.getElementById('totalReviews').textContent =
        overallStats.totalReviews || 0;
    document.getElementById('totalComments').textContent =
        overallStats.totalComments || 0;
    document.getElementById('avgProcessingTime').textContent =
        formatTime(overallStats.averageProcessingTimeMs || 0);

    // Recent activity
    if (recentActivity) {
        document.getElementById('reviewsToday').textContent =
            recentActivity.reviewsToday || 0;
        document.getElementById('reviewsThisWeek').textContent =
            recentActivity.reviewsThisWeek || 0;
        document.getElementById('reviewsThisMonth').textContent =
            recentActivity.reviewsThisMonth || 0;
    }
}

// Update charts
function updateCharts(data) {
    const { severityDistribution, categoryDistribution } = data;

    // Severity chart
    if (severityDistribution) {
        const severityData = {
            labels: ['Info', 'Warning', 'Error'],
            datasets: [{
                data: [
                    severityDistribution.info || 0,
                    severityDistribution.warning || 0,
                    severityDistribution.error || 0
                ],
                backgroundColor: ['#5bc0de', '#f0ad4e', '#d9534f'],
                borderWidth: 0
            }]
        };

        if (severityChart) {
            severityChart.destroy();
        }

        const severityCtx = document.getElementById('severityChart').getContext('2d');
        severityChart = new Chart(severityCtx, {
            type: 'doughnut',
            data: severityData,
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });
    }

    // Category chart
    if (categoryDistribution) {
        const categoryData = {
            labels: Object.keys(categoryDistribution),
            datasets: [{
                label: '이슈 수',
                data: Object.values(categoryDistribution),
                backgroundColor: '#667eea',
                borderColor: '#5568d3',
                borderWidth: 1
            }]
        };

        if (categoryChart) {
            categoryChart.destroy();
        }

        const categoryCtx = document.getElementById('categoryChart').getContext('2d');
        categoryChart = new Chart(categoryCtx, {
            type: 'bar',
            data: categoryData,
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1
                        }
                    }
                }
            }
        });
    }
}

// Load recent reviews
async function loadRecentReviews() {
    try {
        const response = await fetch(`${API_BASE_URL}/api/dashboard/reviews/recent?limit=10`);
        if (!response.ok) throw new Error('Failed to fetch recent reviews');

        const reviews = await response.json();
        displayReviews(reviews);
    } catch (error) {
        console.error('Error loading recent reviews:', error);
        document.getElementById('reviewList').innerHTML =
            '<div class="error">최근 리뷰를 불러오는데 실패했습니다.</div>';
    }
}

// Display reviews
function displayReviews(reviews) {
    const reviewList = document.getElementById('reviewList');

    if (reviews.length === 0) {
        reviewList.innerHTML = '<div class="loading">리뷰가 없습니다.</div>';
        return;
    }

    reviewList.innerHTML = reviews.map(review => `
        <div class="review-item">
            <div class="review-header">
                <span class="review-repo">${review.repositoryOwner}/${review.repositoryName}</span>
                <span class="review-time">${formatDate(review.createdAt)}</span>
            </div>
            <div class="review-title">PR #${review.prNumber}: ${review.prTitle}</div>
            <div class="review-meta">
                <span>코멘트: ${review.totalComments || 0}</span>
                ${formatSeverityCounts(review.severityCounts)}
                <span>처리 시간: ${formatTime(review.processingTimeMs)}</span>
            </div>
        </div>
    `).join('');
}

// Format severity counts
function formatSeverityCounts(counts) {
    if (!counts) return '';

    const badges = [];
    if (counts.error > 0) {
        badges.push(`<span class="badge badge-error">Error: ${counts.error}</span>`);
    }
    if (counts.warning > 0) {
        badges.push(`<span class="badge badge-warning">Warning: ${counts.warning}</span>`);
    }
    if (counts.info > 0) {
        badges.push(`<span class="badge badge-info">Info: ${counts.info}</span>`);
    }

    return badges.join(' ');
}

// Format time in ms to readable format
function formatTime(ms) {
    if (!ms || ms === 0) return '0초';
    if (ms < 1000) return `${ms}ms`;
    return `${(ms / 1000).toFixed(1)}초`;
}

// Format date
function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    const now = new Date();
    const diff = now - date;

    // Less than 1 minute
    if (diff < 60000) return '방금 전';

    // Less than 1 hour
    if (diff < 3600000) {
        const minutes = Math.floor(diff / 60000);
        return `${minutes}분 전`;
    }

    // Less than 24 hours
    if (diff < 86400000) {
        const hours = Math.floor(diff / 3600000);
        return `${hours}시간 전`;
    }

    // Otherwise show date
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Show error message
function showError(message) {
    console.error(message);
}
