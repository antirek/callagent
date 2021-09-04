const express = require('express');
const config = require('config');
const path = require('path');
const fileUpload = require('express-fileupload');
const fs = require('fs');
const useragent = require('express-useragent');
const {slugify} = require('transliteration');

const app = express();
app.use(express.json({limit: '10mb'}));
app.use(useragent.express());

app.get('/', async (req, res) => {
  res.send('OK');
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

app.post('/calldata/:accountId/:mobile/:uuid', async (req, res) => {
  try {
    const {accountId, mobile, uuid} = req.params;
    console.log('POST calldata', {accountId, mobile, uuid});
    console.log('json:', req.body);
  } catch (e) {
    console.log(e);
  }
  res.json({status: 'OK'});
});

app.post('/record/:accountId/:mobile/:uuid',
  fileUpload({
    limits: {
      fileSize: 50 * 1024 * 1024,
    },
  }), async (req, res) => {
  try {
    const {accountId, mobile, uuid} = req.params;
    console.log('POST record', {accountId, mobile, uuid});

    const dir = path.join(config.uploadPath, '/records');
    await fs.promises.mkdir(dir, {recursive: true});
    console.log('records path', dir);
    if (!req.files || !req.files.file) {
      console.log('no file');
      return res.json('no file');
    }
    const filename = slugify(
      req.files.file.name, {
        lowercase: true,
        separator: '_',
      });

    const fname = path.join(dir, mobile + '_' + filename);
    console.log('prepare copy record to file', fname);
    await req.files.file.mv(fname);
    console.log('done');
  } catch (e) {
    console.log('err:', e);
  }
  res.json({status: 'OK'});
});

app.post('/log/:accountId/:mobile',
  fileUpload({
    limits: {
      fileSize: 50 * 1024 * 1024,
    },
  }), async (req, res) => {
  try {
    const {accountId, mobile} = req.params;
    console.log('POST log:', {accountId, mobile});

    const dir = path.join(config.uploadPath, '/logs');
    await fs.promises.mkdir(dir, {recursive: true});
    console.log('logs path', dir);
    if (!req.files || !req.files.file) {
      console.log('no file');
      return res.json('no file');
    }
    const filename = slugify(
      req.files.file.name, {
        lowercase: true,
        separator: '_',
      });

    const fname = path.join(dir, mobile + '_' + filename);
    console.log('prepare copy log to file', fname);
    await req.files.file.mv(fname);
    console.log('done');
  } catch (e) {
    console.log('err:', e);
  }
  res.json({status: 'OK'});
});

app.listen(config.port, () => {
  console.log('start with config', config);
});
