#!/usr/local/bin/node

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

// To make the syntax much cleaner, we abuse the fact that key order is pretty
// much preserved in JS.
const types = {
    UserExpression: {
        type: 'union',
        cases: {
            UserLambda: {
                varName: 'string',
                body: '?UserExpression'
            },
            UserFuncCall: {
                func: 'UserExpression',
                arg: 'UserExpression',
            },
            UserVariable: {
                varName: 'string',
            },
            UserReference: {
                defName: 'string',
            },
        }
    },
    ScreenExpression: {
        expr: 'UserExpression',
        pos: 'CanvasPoint',
    },
    CanvasPoint: {
        type: 'struct',
        fields: {
            canvasX: 'number',
            canvasY: 'number',
        },
    },
    PathComponent: {
        type: 'literal',
        value: "'func' | 'arg' | 'body'",
    },
    ExprPath: {
        type: 'struct',
        fields: {
            exprId: 'number',
            pathSteps: 'Array<PathComponent>',
        }
    }
};

const main = () => {
    const code = joinMap(types, '\n', genType);
    console.log(code);
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
export type ${typeName} {
${genLines((f, t) => `${f}: ${t},`)}
};

export const new${typeName} = (${genComma((f, t) => `${f}: ${t}`)}) => ({
${genLines((f, t) => `${f},`)}
});
`;
};

const genUnion = (typeName, cases) => {
    let result = '';
    result += joinMap(cases, '\n', genUnionCase);
    result += `
export type ${typeName} = ${joinMap(cases, ' | ', (k, v) => k)};
`;
    return result;
};

/**
 * Union cases are almost like structs, but they have a tag that isn't a
 * parameter, so we generate them independently.
 */
const genUnionCase = (caseName, fields) => {
    const tagName = lowerName(caseName);
    const {genLines, genComma} = fieldOperators(fields);
    return `\
export type ${caseName} {
    type: '${tagName}',
${genLines((f, t) => `${f}: ${t},`)}
};

export const new${caseName} = (${genComma((f, t) => `${f}: ${t}`)}) => ({
    type: '${tagName}',
${genLines((f, t) => `${f},`)}
});
`;
};

const fieldOperators = (fields) => ({
    genLines: (transform) => {
        return joinMap(fields, '\n', (k, v) => '    ' + transform(k, v));
    },
    genComma: (transform) => {
        return joinMap(fields, ', ', transform);
    }
});

const lowerName = (name) => {
    return name[0].toLowerCase() + name.slice(1);
};

const joinMap = (obj, sep, transform) => {
    return Object.keys(obj).map(
        (key) => transform(key, obj[key])
    ).join(sep);
};

main();