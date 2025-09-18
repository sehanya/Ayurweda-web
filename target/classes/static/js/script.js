function showTridoshaInfo(tabId) {
  const container = document.querySelector('.Ayurvedic-Tridosha');
  const contents = container.querySelectorAll('.content');
  const buttons = container.querySelectorAll('.tab-button');

  // Remove active classes
  contents.forEach((content) => content.classList.remove('active'));
  buttons.forEach((button) => button.classList.remove('active'));

  // Add active class to selected tab and content
  container.querySelector(`#${tabId}`).classList.add('active');
  container.querySelector(`button[onclick="showTridoshaInfo('${tabId}')"]`).classList.add('active');
}

document.addEventListener('DOMContentLoaded', () => {
    const header = document.querySelector('.header::before');
    const images = [
      'url(../imges/background.webp)',
      'url(../imges/ayurvedic-treatment.png)',
      'url(../imges/woman-3701713_960_720.jpg)',
      'url(../imges/ai-generated-8221928_960_720.webp)'
    ];
    let currentIndex = 0;
  
    setInterval(() => {
      header.style.backgroundImage = images[currentIndex];
      currentIndex = (currentIndex + 1) % images.length; // Loop through images
    }, 5000); // Change every 5 seconds
  });
  function showPanchakarmaInfo(tabId) {
    const container = document.querySelector('.panchakarma');
    const contents = container.querySelectorAll('.panchakarma-content');
    const buttons = container.querySelectorAll('.pachakarma-tab-button');

    // Remove active classes
    contents.forEach((content) => content.classList.remove('active'));
    buttons.forEach((button) => button.classList.remove('active'));

    // Add active class to selected tab and content
    container.querySelector(`#${tabId}`).classList.add('active');
    container.querySelector(`button[onclick="showPanchakarmaInfo('${tabId}')"]`).classList.add('active');
}
  
