var express = require('express');
var http = require('http');
var WebSocket = require('ws');
var WebSocketServer = require('ws').Server, wss = new WebSocketServer({port: 4731});

var allowCrossDomain = function(req, res, next) {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE');
    res.header('Access-Control-Allow-Headers', 'Content-Type');
    next();
};

var app = express();
app.use(allowCrossDomain);
app.use(express.bodyParser());
app.use(express.cookieParser());
app.use(express.session({secret: '2234567890QWERTY'}));
app.use(app.router);


function createGuid(){
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) 
	{
        var r = Math.random()*16|0, v = c === 'x' ? r : (r& 0x3| 0x8);
        return v.toString(16);
    });
}

function checkAuth(req, res, next) {

	console.log(req.body.token);
	 
    var securityToken = JSON.parse(req.body.token);	
	var idCustomer = securityToken.customerId;	

	if(tokens[idCustomer].securityToken == securityToken.securityToken)
    {
		next();
	}	 
	else
	{
		res.send('You are not authorized!');
	}
}


wss.broadcast = function(data) {
    for(var i in this.clients)
        this.clients[i].send(data);
};
 
var maxReservations = 3;
 
var customers = [];
var gadgets = [];
var loans = [];
var reservations = [];
var tokens = {};

customers.push({ name: 'Michael', password: "12345", email: "m@hsr.ch", studentnumber: "10" });
customers.push({ name: 'Sepp', password: "12345", email: "s@hsr.ch", studentnumber: "11" });
customers.push({ name: 'Bob', password: "12345", email: "b@hsr.ch", studentnumber: "12" });
customers.push({ name: 'Iris', password: "12345", email: "i@hsr.ch", studentnumber: "13" });

gadgets.push({ name: 'IPhone 6', manufacturer: "apple", price: 500, inventoryNumber: "20" , condition : "NEW", thumbnailUrl: "http://recodetech.files.wordpress.com/2014/09/iphones-1685.jpg?w=256&h=256&crop=1"});
gadgets.push({ name: 'Raspberry PI', manufacturer: "raspberry", price: 500, inventoryNumber: "21" , condition : "NEW", thumbnailUrl: "https://cdn.sparkfun.com//assets/parts/7/4/9/7/11546-04.jpg"});
gadgets.push({ name: 'Oculus Rift Headset', manufacturer: "Oculus", price: 500, inventoryNumber: "22" , condition : "NEW", thumbnailUrl: "http://assets.vr-zone.net/19610/EcFnHeRGAqCHKRpA_large.jpg"});
gadgets.push({ name: 'Google Glass', manufacturer: "Google", price: 500, inventoryNumber: "23" , condition : "NEW", thumbnailUrl: "http://asset2.cbsistatic.com/cnwk.1d/i/tim2/2013/05/01/35339166-2_610x435.jpg"});
gadgets.push({ name: 'Galaxy S5', manufacturer: "Samsung", price: 500, inventoryNumber: "25" , condition : "NEW", thumbnailUrl: "http://www.chip.de/ii/2/5/3/0/8/7/2/7/S5_Front_800-13025cd1fefde034.jpg"});
gadgets.push({ name: 'HTC One', manufacturer: "HTC", price: 500, inventoryNumber: "26" , condition : "NEW", thumbnailUrl: "http://static.trustedreviews.com/94/000026a22/9a83/IMG-0128.jpg"});
gadgets.push({ name: 'iBeacon', manufacturer: "Altatech", price: 500, inventoryNumber: "27" , condition : "NEW", thumbnailUrl: "http://www.oho.com/sites/default/files/downloads/ibeacon.jpg"});

loans.push({ id: "40", gadgetId: '23', customerId: "10", pickupDate : new Date().toJSON(), returnDate : null});
loans.push({ id: "41", gadgetId: '26', customerId: "10", pickupDate : "2014-08-10T06:10:37.032Z", returnDate : null});
loans.push({ id: "42", gadgetId: '25', customerId: "10", pickupDate : new Date().toJSON(), returnDate : null});

reservations.push({ id: "80", gadgetId: '20', customerId: "10", reservationDate : "2014-08-22T06:10:37.032Z", finished: false});
reservations.push({ id: "81", gadgetId: '21', customerId: "10", reservationDate : "2014-08-22T06:10:37.032Z", finished: false});
reservations.push({ id: "82", gadgetId: '22', customerId: "10", reservationDate : new Date().toJSON(), finished: false});



function addCustomer(customerJson)
{	
	if(findCustomerByEmail( customerJson.email ))
	{
	   return false;	   
	}	
	customers.push(customerJson);	
	wss.broadcast(JSON.stringify({ target: 'customer', type: 'add', data: JSON.stringify(customerJson) }));    
	return true;
}
 
function addReservation(reservationJson)
{	
	console.log("addReservation");
	if(filterReservationsPerCustomer( reservationJson.customerId).length >= maxReservations)
	{
		return false;
	}
	
	if(!isReservedBy(reservationJson.customerId, reservationJson.gadgetId))
	{
		 reservations.push(reservationJson);
		 wss.broadcast(JSON.stringify({ target: 'reservation', type: 'add', data: JSON.stringify(reservationJson) }));     
		 return true;
	}
	return false;
}

 app.post('/customers', function (req, res) {		
    console.log('customers');

    var customerJson = JSON.parse(req.body.value);
	customers.push(customerJson);	
	wss.broadcast(JSON.stringify({ target: 'customer', type: 'add', data: JSON.stringify(customerJson) }));
    res.json(true);
 });

 app.get('/customers', function (req, res) {
     res.json(customers);		 
 })

 
 
function filterLoansPerCustomer(id)
{
	return loans.filter( function(ele) { return ele.customerId == id && ele.returnDate == null;});
} 

function filterReservationsPerCustomer(id)
{
	return reservations.filter( function(ele) { return ele.customerId == id && !ele.finished; });
} 

function sortReservationArrayPerDate(a, b) {
    return new Date(a.pickupDate).getTime() - new Date(b.pickupDate).getTime();
}

function getWaitingPositionOfReservation(reservation)
{
	var sortedReservation =  reservations.filter( function(ele) { return ele.gadgetId == reservation.gadgetId && !ele.finished; }).sort(sortReservationArrayPerDate);
	var realReservation  = getReservationBy(reservation.customerId, reservation.gadgetId);
	
	return sortedReservation.indexOf(realReservation);
} 


 app.post('/gadgets', function (req, res) {
     console.log('gadgets');

     var gadgetsJson = JSON.parse(req.body.value);
     gadgets.push(gadgetsJson);
     wss.broadcast(JSON.stringify({ target: 'gadget', type: 'add', data: JSON.stringify(gadgetsJson)}));

     res.json(true);
 });

 

 app.get('/gadgets', function (req, res) {
     res.json(gadgets);
 });

 app.post('/gadgets/:id', function (req, res) {
     console.log('gadgets/id');
     var gadgedJson = JSON.parse(req.body.value);
     var item = findGadget(req.params.id);
     if (item != null) {
         merge(gadgedJson, item);
         wss.broadcast(JSON.stringify({ target: 'gadget', type: 'update', data: JSON.stringify(item) }));
         res.json(JSON.stringify());
     } else {
         res.json(false);
     }
 });
 


 app.post('/loans', function (req, res) {
     console.log('loans');

     var loanJson = JSON.parse(req.body.value);
     loans.push(loanJson);
     wss.broadcast(JSON.stringify({ target: 'loan', type: 'add', data: JSON.stringify(loanJson) }));
     
     res.json(true);
 });
 

 app.get('/loans', function (req, res) {
     res.json(loans);
 });  
  

 app.post('/loans/:id', function (req, res) {
     console.log('loans/id');
     var gadgedJson = JSON.parse(req.body.value);
     var item = findLoan(req.params.id);
     if (item != null) {
         merge(gadgedJson, item);
         wss.broadcast(JSON.stringify({ target: 'loan', type: 'update', data: JSON.stringify(item) }));
         res.json(JSON.stringify(item));
     } else {
         res.json(false);
     }
 });
 
 
 
 app.post('/reservations', function (req, res) {
     console.log('reservations');
     var reservationJson = JSON.parse(req.body.value);     
     res.json(addReservation(reservationJson));
 });
 
 
 app.get('/reservations', function (req, res) {
     res.json(reservations);
 });  
 
 
 app.post('/reservations/:id', function (req, res) {
     console.log('reservationJson/id');
     var reservationJson = JSON.parse(req.body.value);
     var item = findReservation(req.params.id);
	 
	 if (item != null) {		
         merge(reservationJson, item);
         wss.broadcast(JSON.stringify({ target: 'reservation', type: 'update', data: JSON.stringify(item) }));
         res.json(JSON.stringify(item));
     } else {
         res.json(false);
     }
 });
 
 

app.post('/public/register', function (req, res) {
     console.log('/public/register');
	 var name = req.body.name;	 	
	 var mail = req.body.email;	 	
	 var password = req.body.password;	 	
	 var studentnumber = req.body.studentnumber;	  	 
	 var newStudent = { name: name, password: password, email: mail, studentnumber: studentnumber };
	 
	 res.json(addCustomer( JSON.parse(JSON.stringify(newStudent))));	 
});

 app.post('/public/login', function (req, res) {
     console.log('/public/login');     
	 	 
	 var pwd = req.body.password;	 
	 var mail = req.body.email;	 
	 
     var item = findCustomerByEmail(mail);	 
     if (item != null) {         		
		if(item.password == pwd)
		{
			if(!tokens[item.studentnumber])
			{
				var token = createGuid();				
				tokens[item.studentnumber] = {customerId : item.studentnumber, securityToken : token }; 
			}			
			res.json(tokens[item.studentnumber]);
		}
		else
		{
			res.json("");
		}
     } else {
         res.json("");
     }
 });
 
 
  app.get('/public/reservations', checkAuth, function (req, res) {
 	console.log('/public/reservation');
 	var securityToken = JSON.parse(req.body.token);	
	var idCustomer = securityToken.customerId;	
	var result = JSON.parse(JSON.stringify(filterReservationsPerCustomer( idCustomer )));
	
	
	
	result.forEach(function(entry) {
		entry.gadget = findGadget(entry.gadgetId);
		entry.watingPosition = getWaitingPositionOfReservation(entry);
		entry.isReady = !isLent(entry.gadgetId) && entry.watingPosition == 0;
		
		console.log(entry.watingPosition);		
		delete entry["gadgetId"];
		delete entry["customerId"];
	});

    res.json(result);	
 });
 
 
  app.post('/public/logout', checkAuth, function (req, res) {
     console.log('/public/logout');     
	 	 
	var securityToken = JSON.parse(req.body.token);	
	var idCustomer = securityToken.customerId;	
	 
	if(tokens[idCustomer])
	{
		delete tokens[idCustomer]
		return res.json(true);
	}	
	else
	{
		return res.json(false);
	}	
 });
 
 
 
 app.get('/public/loans', checkAuth, function (req, res) {
 	console.log('/public/loans');
 	var securityToken = JSON.parse(req.body.token);	
	var idCustomer = securityToken.customerId;		
	var result = JSON.parse(JSON.stringify(filterLoansPerCustomer( idCustomer )));
		
	result.forEach(function(entry) {
		entry.gadget = findGadget(entry.gadgetId);
		delete entry["gadgetId"];
		delete entry["customerId"];
	});
	
	res.json(result);		 
 });
 
 

  
 app.del('/public/reservations', checkAuth, function (req, res) {
 	console.log('delete /public/reservations');
 	var securityToken = JSON.parse(req.body.token);	
	var idCustomer = securityToken.customerId;	
	var reservation = findReservation(req.body.id);
	
	if( reservation) 
	{
		reservation.finished = true;
		wss.broadcast(JSON.stringify({ target: 'reservation', type: 'update', data: JSON.stringify(reservation) }));
		res.json(true);
	}
	else
	{
	    res.json(false);
	}	
 });
 
 
 app.post('/public/reservations', checkAuth, function (req, res) {
 	console.log('/public/reservation');
 	var securityToken = JSON.parse(req.body.token);			
	var gadgetId = req.body.gadgetId;
	var idCustomer = securityToken.customerId;	
	
	var reservation = { id: createGuid(), gadgetId: gadgetId, customerId: idCustomer, reservationDate : new Date().toJSON(), finished: false};		
	res.json(addReservation(JSON.parse(JSON.stringify(reservation))));

 });
 

app.get('/public/gadgets', checkAuth, function (req, res) {
	 res.json(gadgets);
});
 
  

 //socket:
app.listen(4730);


function findLoan(id) {
    for (var index = 0; index < loans.length; ++index) {
        var item = loans[index];
        if (item.id === id) {
            return item;
        }
    }
    return null;
}

function isLent(gadgetId) {	
    return getLoanBy(gadgetId) != null;
}


function isReservedBy(customerId, gadgetId) {	
    return getReservationBy(customerId, gadgetId) != null;
}


function getReservationBy(customerId, gadgetId)
{
	for (var index = 0; index < reservations.length; ++index) {
        var item = reservations[index];		
        if (item.customerId == customerId && item.gadgetId == gadgetId && !item.finished ) {
            return item;
        }
    }
	return null;
}


function getLoanBy(gadgetId)
{
	console.log(gadgetId);
	for (var index = 0; index < loans.length; ++index) {
        var item = loans[index];		
		console.log(item);
        if (item.gadgetId == gadgetId && item.returnDate == null ) {
            return item;
        }
    }
	return null;
}



function findReservation(id) {
    for (var index = 0; index < reservations.length; ++index) {
        var item = reservations[index];
        if (item.id === id) {
            return item;
        }
    }
    return null;
}


function findGadget(inventoryNumber) {
    for (var index = 0; index < gadgets.length; ++index) {
        var item = gadgets[index];
        if (item.inventoryNumber === inventoryNumber) {
            return item;
        }
    }
    return null;
}

function findCustomerByEmail(email) {
    for (var index = 0; index < customers.length; ++index) {
        var item = customers[index];
        if (item.email == email) {
            return item;
        }
    }
    return null;
}

function merge(source, target) {
    for (var attrname in source) { target[attrname] = source[attrname]; }
}
