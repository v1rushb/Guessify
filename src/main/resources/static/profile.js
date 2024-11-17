const profileButton = document.getElementById('profile-button');
const profileSection = document.getElementById('profile-section');
const userNameDisplay = document.getElementById('user-name-display');
const userAgeInput = document.getElementById('user-age-input');
const userBioInput = document.getElementById('user-bio-input');
const updateProfileButton = document.getElementById('update-profile-button');

let userId = 1;

async function loadUserProfile() {
    try {
        const response = await fetch(`/guessify/users/details/${userId}`, { method: 'GET' });
        if (!response.ok) throw new Error('Error fetching profile . ');

        const userData = await response.json();
        userNameDisplay.innerText = userData.name;
        userAgeInput.value = userData.age || '';
        userBioInput.value = userData.bio || '';
    } catch (error) {
        console.error('Error loading profile:', error);
        alert('Error loading profile');
    }
}

profileButton.addEventListener('click', () => {
    document.getElementById('signup-section').style.display = 'none';
    document.getElementById('signin-section').style.display = 'none';
    document.getElementById('room-section').style.display = 'none';
    document.getElementById('game-section').style.display = 'none';

    profileSection.style.display = 'block';

    loadUserProfile();});


updateProfileButton.addEventListener('click', async () => {
    const age = userAgeInput.value;
    const bio = userBioInput.value;

    const updatedProfileData = {
        age: age,
        bio: bio
    };

    try {
        const response = await fetch(`/guessify/users/update/${userId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(updatedProfileData)
        });

        if (!response.ok) throw new Error('Error updating profile');
        alert('Profile updated successfully');
    } catch (error) {
        console.error('Error updating profile:', error);
        alert('Failed to update profile');
    }
});
