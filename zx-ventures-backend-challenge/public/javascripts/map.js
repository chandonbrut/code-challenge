function clickFetch(lat,lng) {
    $.ajax({
        url: "/pdv/find",
        type: 'post',
        contentType: 'application/json; charset=UTF-8',
        dataType: 'json',
        data: JSON.stringify({"lat":lat ,"lng":lng}),
        success: function(ret) {

            limpa();

            addPDV(ret);
        }
    });
}

function loadMap() {

    window.map = L.map('map', {
        center: [-23.552657637791054, -46.63764953613281],
        zoom: 12
    });

    window.coverageAreas = [];
    window.pdvs = [];

    map.on('click', function(e) {
      var pt = e;
      clickFetch(e.latlng.lat,e.latlng.lng);
    });


    L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 20,
      minZoom: 2
    }).addTo(window.map);


}

function addPDV(ret) {
    var pdv = L.geoJson(ret.address).addTo(window.map)
    pdv.bindPopup('<strong>'+ret.tradingName+'</strong>');
    window.pdvs.push(pdv);

    var area = L.geoJson(ret.coverageArea).addTo(window.map);
    area.on('click',function(c){console.log(c); clickFetch(c.latlng.lat,c.latlng.lng); });
    window.coverageAreas.push(area);
}

function fetchAllPDVs() {
    $.ajax({
        url: "/pdvs",
        type: 'get',
        contentType: 'application/json; charset=UTF-8',
        dataType: 'json',
        success: function(pdvs) {
            limpa();
            pdvs.forEach(function(ret) { addPDV(ret) });
        }
    });
}
function limpa(){
    window.coverageAreas.forEach((area) => window.map.removeLayer(area));
    window.pdvs.forEach((pdv) => window.map.removeLayer(pdv));

    window.coverageAreas.length=0;
    window.pdvs.length=0;
}