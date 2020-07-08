// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random greeting to the page.
 */
function addRandomGreeting() {
  const greetings =
      ['Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!'];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}

async function getRandomMyNameUsingAsyncAwait() {
  const response = await fetch('/data');
  const name = await response.text();
  document.getElementById('name-container').innerText = name;
}

function getJSONString() {
  fetch('/data').then(response => response.json()).then((messages) => {
    // Stats is an object, not a string, so we have to
    // reference its fields to create HTML content.

    const jsonListElement = document.getElementById('json-string');
    jsonListElement.innerHTML = '';
    for (const elem of messages) {
      jsonListElement.appendChild(createListElement(elem));
    } 
  });
}

function loadingMyComments() {
    // Stats is an object, not a string, so we have to
    // reference its fields to create HTML content.
    let elem = parseInt(document.getElementById('amount').value, 10);
    if (elem != 0) {
      fetch(`/data?amount=${elem}`).then(response => response.json()).then((jsonVersion) => {
        const oneComment = document.getElementById('log');
        for (let i = 0; i < elem; i++) {
          oneComment.appendChild(createListElement(jsonVersion[i]));
        }
      });
    }
}

function deleteMyComments() {
  fetch('/delete-data', {method: 'POST'}).then(response => response.text()).then((worked) => {
    loadingMyComments();
  });
}

/** Creates an <li> element containing text. */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}

function visualizeMap() {
  // Establishes the styles I want added to my map. 
  var stylesMap = new google.maps.StyledMapType(
    [ {
        elementType: 'labels.text.fill',
        stylers: [
          {color: '#FA8072'}   
        ]
      },
      {
        elementType: 'labels.text.stroke',
        stylers: [
          {color: '#F8F8FF'}
        ]    
      },
      {
        featureType: 'road',
        elementType: 'geometry.fill',
        stylers: [
          {color: '#FFFAF0'}
        ]  
      },
      {
        featureType: 'road',
        elementType: 'geometry.stroke',
        stylers: [
          {color: '#A9A9A9'}
        ]  
      },
      {
        featureType: 'water',
        elementType: 'geometry.fill',
        stylers: [
          {color: '#48D1CC'}
        ]
      },
      {
        featureType: 'water',
        elementType: 'geometry.stroke',
        stylers: [
          {color: '#000000'}
        ]
      },
      {
        featureType: 'poi',
        elementType: 'geometry.fill',
        stylers: [
          {color: '#FFD700'}
        ]
      }
    ],

    {name: 'Styles Map'});

  var myHome = {
                 lat: 10.677693, 
                 lng: -71.625913
  };  
  var marLake = {
                  lat: 9.815833, 
                 lng: -71.556667
  };

  // Initializes map.
  const map = new google.maps.Map(
    document.getElementById('map'),
    {
      center: {
        lat: 9.815833, 
        lng: -71.556664
      }, 
      zoom: 7,
      mapTypeControlOptions: {
        mapTypeIds: ['hybrid', 'styles_map']
      }
  });
  
  //Marker positioned at my house. 
  var markerHome = new google.maps.Marker({
    position: myHome,
    map: map,
    title: 'My house!'
  });

  // Marker positioned at Maracaibo Lake.
  var markerLake = new google.maps.Marker({
    position: marLake,
    map: map,
    title: 'Maracaibo Lake'
  });

  // Info windows for markers.
  const homeInfoWindow = new google.maps.InfoWindow({
    content: 'Lived here until I was 18 years old in the 12th floor.' 
  });
  
  const lakeInfoWindow = new google.maps.InfoWindow({
    content: 'The weather phenomenon known as the Catatumbo lightning at ' +
      'Lake Maracaibo regularly produces more lightning than any other place on the planet.'
  });
  
  map.mapTypes.set('styles_map', stylesMap);
  map.setMapTypeId('styles_map');
  homeInfoWindow.open(map, markerHome);
  lakeInfoWindow.open(map, markerLake);

  favoritePlaces();
}

function favoritePlaces() {
  fetch('/interactive').then(response => response.json()).then((locations) => {
    const favoriteMap = new google.maps.Map(
      document.getElementById('mapTwo'),
      {
        center: {
          lat: 9.815833, 
          lng: -71.556664
        }, 
        zoom: 7,
    });
    locations.forEach((location) => {
      new google.maps.Marker(
        {position: {lat: location.lat, lng: location.lng}, map:favoriteMap});
    });
  });
}