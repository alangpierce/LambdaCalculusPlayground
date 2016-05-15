/**
 * Code generator for the immutable types used by the library. We generate these
 * as a build step and check them into the repo to make the development
 * experience a little IDE-friendly and typesafe. Maybe eventually Flow will be
 * expressive enough to get everything we want, but not yet.
 *
 * TODO: Investigate if this could be done as a babel transform. Also check if
 * another thing like this already exists. Probably so, but it probably isn't
 * quite what I want, e.g. it probably wouldn't support unions like I want them.
 */
"use strict";

export const generateTypes = (types) => {
    return `\
/**
 * Autogenerated; do not edit! Run "npm gen-types" to regenerate.
 *
 * @flow
 */
 
import {IList, IMap, ISet} from './types-collections'
import {buildUnionCaseClass, buildValueClass} from './types-lib'
import type {Updater} from './types-lib'

${joinMap(types, '\n', genType)}
`;
};

const genType = (typeName, typeData) => {
    const lowerTypeName = lowerName(typeName);
    if (lowerTypeName === typeName) {
        throw new Error('Type names must be upper-case.');
    }

    if (typeData.type === 'literal') {
        return genLiteral(typeName, typeData.value);
    } else if (typeData.type === 'struct') {
        return genStruct(typeName, typeData.fields);
    } else if (typeData.type === 'union') {
        return genUnion(typeName, typeData.cases);
    }
};

const genLiteral = (typeName, value) => {
    return `\
export type ${typeName} = ${value};
`;
};

const genStruct = (typeName, fields) => {
    const {genLines, genComma} = fieldOperators(fields);
    return `\
const ${typeName}Impl = buildValueClass('${typeName}', [${genComma((f) => `'${f}'`)}]);

export type ${typeName} = {
${genLines((f, t) => `${f}: ${t},`)}\
${genLines((f, t) => `with${upperName(f)}(${f}: ${t}): ${typeName},`)}\
${genLines((f, t) => `update${upperName(f)}(updater: Updater<${t}>): ${typeName},`)}\
    toJS(): any,
    serialize(): any,
};

export const new${typeName} = (${genComma((f, t) => `${f}: ${t}`)}): ${typeName} => (new ${typeName}Impl({
${genLines((f, t) => `${f},`)}\
}));
`;
};

const genUnion = (typeName, cases) => {
    let result = '';
    result += joinMap(cases, '\n',
        (caseName, fields) => genUnionCase(typeName, caseName, fields));

    result += `
export type ${typeName} = ${joinMap(cases, ' | ', (caseName) => caseName)};

export type ${typeName}Visitor<T> = {
${joinMap(cases, '\n', (caseName) => `\
    ${lowerName(caseName)}(${lowerName(caseName)}: ${caseName}): T,`)}
}
`;
    return result;
};

/**
 * Union cases are almost like structs, but they have a tag that isn't a
 * parameter, so we generate them independently.
 *
 * Also, we need to handle a special case where Redux actions need to be plain
 * objects.
*/
const genUnionCase = (unionName, caseName, fields) => {
    const tagName = lowerName(caseName);
    const {genLines, genComma} = fieldOperators(fields);
    return `\
const ${caseName}Impl = buildUnionCaseClass('${tagName}', [${genComma((f) => `'${f}'`)}]);

export type ${caseName} = {
    type: '${tagName}',
${genLines((f, t) => `${f}: ${t},`)}\
${genLines((f, t) => `with${upperName(f)}(${f}: ${t}): ${caseName},`)}\
${genLines((f, t) => `update${upperName(f)}(updater: Updater<${t}>): ${caseName},`)}\
    match<T>(visitor: ${unionName}Visitor<T>): T,
    toJS(): any,
    serialize(): any,
};

export const new${caseName} = (${genComma((f, t) => `${f}: ${t}`)}): ${caseName} => (new ${caseName}Impl({
    type: '${tagName}',
${genLines((f, t) => `${f},`)}\
}));
`;
};

const fieldOperators = (fields) => ({
    genLines: (transform) => {
        return joinMap(fields, '', (k, v) => '    ' + transform(k, v) + '\n');
    },
    genComma: (transform) => {
        return joinMap(fields, ', ', transform);
    }
});

const lowerName = (name) => {
    return name[0].toLowerCase() + name.slice(1);
};

const upperName = (name) => {
    return name[0].toUpperCase() + name.slice(1);
};

const joinMap = (obj, sep, transform) => {
    return Object.keys(obj).map(
        (key) => transform(key, obj[key])
    ).join(sep);
};

export const generateWebstormTypes = (types) => {
    return `\
/**
 * Autogenerated; do not edit! Run "npm gen-types" to regenerate.
 *
 * This is a bit of a hack: WebStorm doesn't full recognize all Flow types, but
 * it matches types by name, so we can refine types to be more WebStorm-friendly
 * by including this file in the project.
 */
${joinMap(types, '', genWebstormType)}
`;
};

const genWebstormType = (typeName, typeData) => {
    if (typeData.type === 'union') {
        return genWebstormUnion(typeName, typeData);
    }
    return '';
};

const genWebstormUnion = (typeName, typeData) => {
    const {cases} = typeData;
    // Show the expanded type because that's more useful in WebStorm.
    return `
type ${typeName} = {
    match<T>(visitor: {
${joinMap(cases, '\n', (caseName) => `\
        ${lowerName(caseName)}(${lowerName(caseName)}: ${caseName}): T,`)}
    }): T,
};
`
};