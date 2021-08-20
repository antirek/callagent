const express = require('express');
const config = require('config');
const path = require('path');
const fileUpload = require('express-fileupload');
const fs = require('fs');
const useragent = require('express-useragent');

const app = express();
app.use(express.json({limit: '10mb'}));
app.use(useragent.express());

app.post('/record/:accountId/:mobile/:uuid',
    fileUpload(), async (req, res) => {
      try {
        const {accountId, mobile, uuid} = req.params;
        console.log('POST record', {accountId, mobile, uuid});
      } catch (e) {
        console.log('err:', e);
      }
      res.json({status: 'OK'});
    });

app.post('/calldata/:accountId/:mobile/:uuid', async (req, res) => {
  try {
    const {accountId, mobile, uuid} = req.params;
    console.log('POST calldata', {accountId, mobile, uuid});
  } catch (e) {
    console.log(e);
  }
  res.json({status: 'OK'});
});

app.post('/event/:accountId/:mobile/:uuid', async (req, res) => {
  try {
    const {uuid, accountId, mobile} = req.params;
    console.log('POST event', {accountId, mobile, uuid});
    console.log('json:', req.body);
  } catch (e) {
    console.log('err', e);
  }
  res.json({status: 'OK'});
});

app.post('/log/:accountId/:mobile',
    fileUpload(), async (req, res) => {
    try {
      const {accountId, mobile} = req.params;
      console.log('POST log:', {accountId, mobile});      
    } catch (e) {
      console.log('err:', e);
    }
    res.json({status: 'OK'});
  });

app.listen(config.port, () => {
  console.log('start with config', config);
});
