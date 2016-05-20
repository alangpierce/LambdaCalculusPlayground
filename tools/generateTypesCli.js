#!/usr/local/bin/babel-node

import fs from 'fs'

import {
    generateTypes,
    generateFlowTypes,
    generateWebstormTypes,
    generateMixinTemplates,
    generateFlowMixinTemplates
} from './TypeGenerator'
import typeDefs from '../typeDefs'

const main = () => {
    const typesFileStr = generateTypes(typeDefs);
    fs.writeFile(__dirname + '/../types.js', typesFileStr, (err) => {
        if (err) {
            console.log('Error: ' + err);
        } else {
            console.log('Wrote types file!');
        }
    });

    const flowTypesFileStr = generateFlowTypes(typeDefs);
    fs.writeFile(__dirname + '/../types.js.flow', flowTypesFileStr, (err) => {
        if (err) {
            console.log('Error: ' + err);
        } else {
            console.log('Wrote flow types file!');
        }
    });

    const webstormTypesFileStr = generateWebstormTypes(typeDefs);
    fs.writeFile(__dirname + '/../webstorm-types.js', webstormTypesFileStr, (err) => {
        if (err) {
            console.log('Error writing webstorm file: ' + err);
        } else {
            console.log('Wrote WebStorm types file!');
        }
    });

    const mixinTemplatesStr = generateMixinTemplates(typeDefs);
    fs.writeFile(__dirname + '/../types-mixin-templates.js', mixinTemplatesStr, (err) => {
        if (err) {
            console.log('Error writing mixin templates file: ' + err);
        } else {
            console.log('Wrote mixin templates file!');
        }
    });

    const flowMixinTemplatesStr = generateFlowMixinTemplates(typeDefs);
    fs.writeFile(__dirname + '/../types-mixin-templates.js.flow', flowMixinTemplatesStr, (err) => {
        if (err) {
            console.log('Error writing flow mixin templates file: ' + err);
        } else {
            console.log('Wrote flow mixin templates file!');
        }
    });
};

main();