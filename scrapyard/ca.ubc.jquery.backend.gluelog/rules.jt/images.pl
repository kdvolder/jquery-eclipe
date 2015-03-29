%baseImage(X, Warning) :- Warning(X).
%baseImage(X, Error) :- Error(X).
%baseImage(X, Bookmark) :- Bookmark(X).
%baseImage(X, Task) :- Task(X).
%baseImage(X, TVar) :- typevar(X).
baseImage(X, Img) :- baseImageDescriptor(X,Desc),descriptorImg(Desc,Img),!.
%baseImage(X,'Feature.gif') :- subject(X).

% 
% Helpers
%
visibilityModifier(X,'public') :- public(X),!.
visibilityModifier(X,'protected') :- protected(X),!.
visibilityModifier(X,'private') :- private(X),!.
visibilityModifier(_,'default').

classOrInterface(X,'interface') :- interface(X),!.
classOrInterface(X,'enum') :- enum(X),!.
classOrInterface(_,'class').

innerOrOuter(X,N) :- classDefT(X,Pid,_,_),((classDefT(Pid,_,_,_),(N='inner'));(N='outer')).

returnTypeName(X,R) :- methodTypeJQ(X,R).

fieldTypeName(X,R) :- fieldDefT(X,_,T,_,_),getTypeName(T,R).

methodOrField(X,'method') :- method(X);constructor(X).
methodOrField(X,'field') :- field(X).

elementType(X,'field') :- field(X),!.
elementType(X,'constructor') :- constructor(X),!.
elementType(X,'initializer') :- initializer(X),!.
elementType(X,'method') :- method(X),!.
elementType(X,'interface') :- interface(X),!.
elementType(X,'enum') :- enum(X),!.
elementType(X,'class') :- class(X),!.
elementType(X,'package') :- package(X),!.
elementType(X,'compilationUnit') :- compilationUnit(X),!.
elementType(_,'other').

%
% baseImageDescriptor/2
%
% PACKAGE baseImageDescriptor
baseImageDescriptor(X,['package']) :- packageT(X,_),!.

% Compilation Unit baseImageDescriptor
baseImageDescriptor(X,['compilationUnit']) :- compilationUnit(X),fileName(X,N),javaFile(N),!.
baseImageDescriptor(X,['compilationUnitClassFile']) :- compilationUnit(X),fileName(X,N),classFile(N),!.

%Class baseImageDescriptor
baseImageDescriptor(X,[CorI,InnerOrOuter,Visib]) :- 
	classOrInterface(X,CorI), innerOrOuter(X,InnerOrOuter), 
	visibilityModifier(X,Visib),!.

% FIELD/METHOD baseImageDescriptor
baseImageDescriptor(X,['field','public']) :- enumConstantT(X,_,_,_,_).
baseImageDescriptor(X,[MethodOrField,Visib]) :- methodOrField(X,MethodOrField),visibilityModifier(X,Visib),!.

% Initializer baseImageDescriptor
baseImageDescriptor(X,['initializer']) :- initializer(X),!.

% Import baseImageDescriptor
baseImageDescriptor(X,['import']) :- importT(X,_,_),!.

%
%	descriptorImg/2
%

% BASE IMAGES
descriptorImg(['compilationUnit'], 'DESC_OBJS_CUNIT').
descriptorImg(['compilationUnitClassFile'], 'DESC_OBJS_CFILE').
descriptorImg(['package'], 'DESC_OBJS_PACKAGE').
descriptorImg(['initializer'], 'DESC_MISC_PRIVATE').
descriptorImg(['import'], 'DESC_OBJS_IMPDECL').
% descriptorImg(['import'], 'DESC_OBJS_IMPCONT').

% OUTER CLASSES
descriptorImg(['class','outer','public'],'DESC_OBJS_CLASS').
descriptorImg(['class','outer','protected'],'DESC_OBJS_CLASS'). % valid case?
descriptorImg(['class','outer','private'],'DESC_OBJS_CLASS').
descriptorImg(['class','outer','default'],'DESC_OBJS_CLASS_DEFAULT').

% INNER CLASSES
descriptorImg(['class','inner','public'],'DESC_OBJS_INNER_CLASS_PUBLIC').
descriptorImg(['class','inner','protected'],'DESC_OBJS_INNER_CLASS_PROTECTED').
descriptorImg(['class','inner','private'],'DESC_OBJS_INNER_CLASS_PRIVATE').
descriptorImg(['class','inner','default'],'DESC_OBJS_INNER_CLASS_DEFAULT').

% OUTER INTERFACES
descriptorImg(['interface','outer','public'],'DESC_OBJS_INTERFACE').
descriptorImg(['interface','outer','protected'],'DESC_OBJS_INTERFACE'). % valid case?
descriptorImg(['interface','outer','private'],'DESC_OBJS_INTERFACE').
descriptorImg(['interface','outer','default'],'DESC_OBJS_INTERFACE_DEFAULT').

% INNER INTERFACES
descriptorImg(['interface','inner','public'],'DESC_OBJS_INNER_INTERFACE_PUBLIC').
descriptorImg(['interface','inner','protected'],'DESC_OBJS_INNER_INTERFACE_PROTECTED').
descriptorImg(['interface','inner','private'],'DESC_OBJS_INNER_INTERFACE_PRIVATE').
descriptorImg(['interface','inner','default'],'DESC_OBJS_INNER_INTERFACE_DEFAULT').

% ENUMS
descriptorImg(['enum','inner','public'],'DESC_OBJS_ENUM').
descriptorImg(['enum','inner','protected'],'DESC_OBJS_ENUM_PROTECTED').
descriptorImg(['enum','inner','private'],'DESC_OBJS_ENUM_PRIVATE').
descriptorImg(['enum','inner','default'],'DESC_OBJS_ENUM_DEFAULT').

% METHODS
descriptorImg(['method','public'], 'DESC_MISC_PUBLIC').
descriptorImg(['method','protected'], 'DESC_MISC_PROTECTED').
descriptorImg(['method','private'], 'DESC_MISC_PRIVATE').
descriptorImg(['method','default'], 'DESC_MISC_DEFAULT').

% FIELDS
descriptorImg(['field','public'], 'DESC_FIELD_PUBLIC').
descriptorImg(['field','protected'], 'DESC_FIELD_PROTECTED').
descriptorImg(['field','private'], 'DESC_FIELD_PRIVATE').
descriptorImg(['field','default'], 'DESC_FIELD_DEFAULT').
