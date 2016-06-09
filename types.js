/**
 * Autogenerated; do not edit! Run "npm gen-types" to regenerate.
 */
import {buildUnionCaseClass, buildValueClass} from './types-lib';
import * as mixins from './types-mixins';

export const State = buildValueClass('State', null, ['canvasExpressions', 'nextExprId', 'canvasDefinitions', 'definitions', 'pendingResults', 'activeDrags', 'highlightedExprs', 'highlightedEmptyBodies', 'highlightedDefinitionBodies', 'paletteState']);
export const Reset = buildUnionCaseClass('reset', []);
export const ToggleLambdaPalette = buildUnionCaseClass('toggleLambdaPalette', []);
export const ToggleDefinitionPalette = buildUnionCaseClass('toggleDefinitionPalette', []);
export const AddExpression = buildUnionCaseClass('addExpression', ['canvasExpr']);
export const PlaceDefinition = buildUnionCaseClass('placeDefinition', ['defName', 'screenPos']);
export const DeleteDefinition = buildUnionCaseClass('deleteDefinition', ['defName']);
export const MoveExpression = buildUnionCaseClass('moveExpression', ['exprId', 'pos']);
export const DecomposeExpressionAction = buildUnionCaseClass('decomposeExpressionAction', ['path', 'targetPos']);
export const InsertAsArg = buildUnionCaseClass('insertAsArg', ['argExprId', 'path']);
export const InsertAsBody = buildUnionCaseClass('insertAsBody', ['bodyExprId', 'path']);
export const EvaluateExpression = buildUnionCaseClass('evaluateExpression', ['exprId']);
export const PlacePendingResult = buildUnionCaseClass('placePendingResult', ['exprId', 'width', 'height']);
export const FingerDown = buildUnionCaseClass('fingerDown', ['fingerId', 'screenPos']);
export const FingerMove = buildUnionCaseClass('fingerMove', ['fingerId', 'screenPos']);
export const FingerUp = buildUnionCaseClass('fingerUp', ['fingerId', 'screenPos']);
export const Lambda = buildUnionCaseClass('lambda', ['varName', 'body']);
export const FuncCall = buildUnionCaseClass('funcCall', ['func', 'arg']);
export const Variable = buildUnionCaseClass('variable', ['varName']);
export const EvalLambda = buildUnionCaseClass('evalLambda', ['varMarker', 'originalVarName', 'body']);
export const EvalFuncCall = buildUnionCaseClass('evalFuncCall', ['func', 'arg']);
export const EvalBoundVariable = buildUnionCaseClass('evalBoundVariable', ['slot']);
export const EvalUnboundVariable = buildUnionCaseClass('evalUnboundVariable', ['varMarker', 'originalVarName']);
export const EvalFreeVariable = buildUnionCaseClass('evalFreeVariable', ['varName']);
export const UserLambda = buildUnionCaseClass('userLambda', ['varName', 'body']);
export const UserFuncCall = buildUnionCaseClass('userFuncCall', ['func', 'arg']);
export const UserVariable = buildUnionCaseClass('userVariable', ['varName']);
export const UserReference = buildUnionCaseClass('userReference', ['defName']);
export const CanvasExpression = buildValueClass('CanvasExpression', null, ['expr', 'pos']);
export const PendingResult = buildValueClass('PendingResult', null, ['expr', 'sourceExprId']);
export const DisplayState = buildValueClass('DisplayState', null, ['screenExpressions', 'screenDefinitions', 'paletteState', 'measureRequests', 'definitionNames', 'isDragging', 'isDraggingExpression']);
export const PaletteDisplayState = buildValueClass('PaletteDisplayState', null, ['activePalette', 'lambdas', 'definitions']);
export const MeasureRequest = buildValueClass('MeasureRequest', null, ['expr', 'resultHandler']);
export const ScreenDefinition = buildValueClass('ScreenDefinition', null, ['defName', 'expr', 'pos', 'defKey', 'refKey', 'emptyBodyKey', 'shouldHighlightEmptyBody', 'key', 'isDragging']);
export const ScreenExpression = buildValueClass('ScreenExpression', null, ['expr', 'pos', 'key', 'isDragging', 'executeHandler']);
export const DisplayLambda = buildUnionCaseClass('displayLambda', ['exprKey', 'shouldHighlight', 'varKey', 'emptyBodyKey', 'shouldHighlightEmptyBody', 'varName', 'body']);
export const DisplayFuncCall = buildUnionCaseClass('displayFuncCall', ['exprKey', 'shouldHighlight', 'func', 'arg']);
export const DisplayVariable = buildUnionCaseClass('displayVariable', ['exprKey', 'shouldHighlight', 'varName']);
export const DisplayReference = buildUnionCaseClass('displayReference', ['exprKey', 'shouldHighlight', 'shouldShowError', 'defName']);
export const CanvasPoint = buildValueClass('CanvasPoint', null, ['canvasX', 'canvasY']);
export const PointDifference = buildValueClass('PointDifference', null, ['dx', 'dy']);
export const ScreenPoint = buildValueClass('ScreenPoint', mixins.ScreenPointMixin, ['screenX', 'screenY']);
export const ScreenRect = buildValueClass('ScreenRect', mixins.ScreenRectMixin, ['topLeft', 'bottomRight']);
export const ExprPath = buildValueClass('ExprPath', null, ['container', 'pathSteps']);
export const ExprIdContainer = buildUnionCaseClass('exprIdContainer', ['exprId']);
export const DefinitionContainer = buildUnionCaseClass('definitionContainer', ['defName']);
export const PickUpExpression = buildUnionCaseClass('pickUpExpression', ['exprId', 'offset', 'screenRect']);
export const PickUpDefinition = buildUnionCaseClass('pickUpDefinition', ['defName', 'offset', 'screenRect']);
export const ExtractDefinition = buildUnionCaseClass('extractDefinition', ['defName', 'offset', 'screenRect']);
export const DecomposeExpression = buildUnionCaseClass('decomposeExpression', ['exprPath', 'offset', 'screenRect']);
export const CreateExpression = buildUnionCaseClass('createExpression', ['expr', 'offset', 'screenRect']);
export const StartPan = buildUnionCaseClass('startPan', ['startPos']);
export const DragData = buildValueClass('DragData', null, ['payload', 'grabOffset', 'screenRect']);
export const DraggedExpression = buildUnionCaseClass('draggedExpression', ['userExpr']);
export const DraggedDefinition = buildUnionCaseClass('draggedDefinition', ['defName']);
export const AddToTopLevelResult = buildUnionCaseClass('addToTopLevelResult', ['payload', 'screenPos']);
export const InsertAsBodyResult = buildUnionCaseClass('insertAsBodyResult', ['lambdaPath', 'expr']);
export const InsertAsArgResult = buildUnionCaseClass('insertAsArgResult', ['path', 'expr']);
export const InsertAsDefinitionResult = buildUnionCaseClass('insertAsDefinitionResult', ['defName', 'expr']);
export const RemoveResult = buildUnionCaseClass('removeResult', []);
export const ExpressionKey = buildUnionCaseClass('expressionKey', ['exprPath']);
export const EmptyBodyKey = buildUnionCaseClass('emptyBodyKey', ['lambdaPath']);
export const LambdaVarKey = buildUnionCaseClass('lambdaVarKey', ['lambdaPath']);
export const DefinitionKey = buildUnionCaseClass('definitionKey', ['defName']);
export const DefinitionRefKey = buildUnionCaseClass('definitionRefKey', ['defName']);
export const DefinitionEmptyBodyKey = buildUnionCaseClass('definitionEmptyBodyKey', ['defName']);
export const PaletteLambdaKey = buildUnionCaseClass('paletteLambdaKey', ['varName']);
export const PaletteReferenceKey = buildUnionCaseClass('paletteReferenceKey', ['defName']);
