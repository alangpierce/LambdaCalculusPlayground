#!/usr/local/bin/babel-node

import fs from 'fs'

import generateTypes from './generateTypes'
import typeDefs from '../typeDefs'

const main = () => {
    const typesFileStr = generateTypes(typeDefs);
    fs.writeFile('../types.js', typesFileStr, (err) => {
        if (err) {
            console.log('Error: ' + err);
        } else {
            console.log('Done!');
        }
    });
};

main();