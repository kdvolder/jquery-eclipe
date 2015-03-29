%-----------------------------------------------------------------------------
%
% General
%
%-----------------------------------------------------------------------------
outputId(X,N) :- concat_atom(['id:',X],N).

% JQuery->JavaModel elements query
% 
% Helps convert JQuery database facts to eclipse JavaModel elements
findPackage(C,P) :- classDefT(C,P,_,_),package(P).
findPackage(C,P) :- anonymousClass(C,_),
	classDefT(C,N,_,_),newClassT(N,_,X,_,_,_,_,_),child(Y,X),findPackage(Y,P).

jq2jm(X,Type,Lbl,Pkg,Parent) :- package(X),
    elementType(X,Type),label(X,Lbl),
	(Pkg=Lbl),
	(Parent='').

jq2jm(X,Type,Lbl,Pkg,Parent) :- typeJQ(X),
    elementType(X,Type),label(X,Lbl),
	(package(P,X),label(P,Pkg)),
	(Parent='').

jq2jm(X,Type,Lbl,Pkg,Parent) :- method(X),
    elementType(X,Type),label(X,Lbl),
	(child(C,X),label(C,Parent)),
	(package(P,C),label(P,Pkg)).

jq2jm(X,Type,Lbl,Pkg,Parent) :- field(X),
    elementType(X,Type),label(X,Lbl),
	(child(C,X),label(C,Parent)),
	(package(P,C),label(P,Pkg)).


% listRef
listRef(P,[_|R],O) :- listRef(P - 1,R,O).
listRef(0,[],_). 

%
% Location stuff
locationJQfinder(T,St,Len,X) :- 	
	( child(T,Y),(locationJQfinder(Y,St,Len,X); (X=Y)) ; (X=T) ),
	slT(X,O,L),sum(St,Len,OE),(St > O),sum(O,L,E),(E > OE).
locationJQ(X,File,_,_) :- var(X),fileT(_,_,File,Defs),member(X,Defs),typeJQ(X).
locationJQ(X,File,St,Len) :- var(X),fileT(_,_,File,Defs),member(T,Defs),
	locationJQfinder(T,St,Len,X).

% This query would be great... but it's sometimes slow!!
% locationJQ(X,File,St,Len) :- var(X),sl_argT(X,_,St,Len),sourceLocation(X,File,_,_),!.

locationJQ(X,File,St,Len) :- sourceLocation(X,File,_,_),sl_argT(X,_,St,Len),!.
locationJQ(X,File,St,Len) :- sourceLocation(X,File,St,Len),!.
locationJQ(X,File,St,Len) :- anonymousClass(X,_),classDefT(X,P,_,_),locationJQ(P,File,St,Len),!.

%
% Label stuff
label(X,N) :- labelQuery(X,N).

getSymbolName(X,N) :-
	(localDefT(X,_,_,_,N,_),!); % local variable 
	(fieldDefT(X,_,_,N,_),!); 	% field          
	(paramDefT(X,_,_,N),!); 	% parameter      
	(methodDefT(X,_,N,_,_,_,_),!); % method         
	(classDefT(X,_,N,_),!); 	% class          
	(packageT(X,N)). 			% package        

% This rule helps with reverse labelling.  
labelQuery(X,N) :- 
	% method name
	(var(X),methodName(X,N),!);
	% field name
	(var(X),fieldDefT(X,_,_,N,_),!).

labelQuery(X,N) :- 
	% TypeInfo (only if X is bound)
	(nonvar(X),getTypeName(X,N),!);
	% file name
	(fileT(X,_,T,_),parseFileName(T,N),!);
	% Package
	(packageT(X,N),!);

	% Method Name
	(methodSignature(X,N),!);
	% Method Call
	(callT(X,_,Parent,_,M,_,_),methodDefT(Parent,_,Name,_,_,_,_),concat_atom([Name,' calls ',M],N),!);
	% Constructor calls
	(newClassT(X,_,M,C,_,_,_,_),methodDefT(C,Ct,_,_,_,_,_),classDefT(Ct,_,Target,_),methodDefT(M,_,Name,_,_,_,_),concat_atom([Name,' calls new ',Target],N),!);

	% Field reads/writes
	(reads(M,F,X),methodDefT(M,_,MName,_,_,_,_),fieldDefT(F,_,_,FName,_),concat_atom([MName,' reads ',FName],N),!);
	(writes(M,F,X),methodDefT(M,_,MName,_,_,_,_),fieldDefT(F,_,_,FName,_),concat_atom([MName,' writes ',FName],N),!);
	
	% anonymous class
	(anonymousClass(X,Parent),getSymbolName(Parent,T),concat_atom(['new ',T,' {...}'],N),!);
	% anonymous class creation
	(newClassT(X,_,_,_,_,_,T,_),label(T,N),!);
	
	% parameters
	(paramDefT(X,P,T,_),catchT(P,_,M,X,_),methodName(M,MName),label(T,Tn),concat_atom([MName,' catches ',Tn],N),!);
	
	% instance of
	(typeTestT(X,_,M,T,_),methodName(M,MName),label(T,Tn),concat_atom([MName,' calls instanceof ',Tn],N),!);
	
	% literals
	(literalT(X,_,_,_,N),!);
	
	% handle enums here because getSymbolName/2 does do it
	(enumConstantT(X,_,_,N,_),!);
	
	% imports
	% (importT(X,_,Y),fullQualifiedName(Y,N),!);

	% Generic...
	(getSymbolName(X,N)).

%-----------------------------------------------------------------------------
%
% Method Signature
%
%-----------------------------------------------------------------------------
% gets method type name
methodTypeJQ(X,Result) :-
	not(initializer(X)),methodDefT(X,_,_,_,T,_,_),getTypeName(T,Result).

methodName(X,P,N) :- constructor(X),
	methodDefT(X,C,_,P,_,_,_),classDefT(C,_,N,_).

% generates a method name in the form of name(argtype0, argtype1, argtype2)
methodSignature(X,N) :- methodName(X,P,MName),
	types(P,ParamTypes),
	findall(Type,generateType(ParamTypes,Type),ParamList),
	concat_atom(ParamList,',',ParamString),
	concat_atom([MName,'(',ParamString,')'],N).

methodSignature(X,N) :- 
	methodDefT(X,_,MName,P,_,_,_),
	types(P,ParamTypes),
	findall(Type,generateType(ParamTypes,Type),ParamList),
	concat_atom(ParamList,',',ParamString),
	concat_atom([MName,'(',ParamString,')'],N).


% check method signature
strongLikeThis(M1,M2) :- 
	methodDefT(M1,_,Name,P1,Type,Exception,_),
	methodDefT(M2,_,Name,P2,Type,Exception,_),
	types(P1,Params1),types(P2,Params2),(Params1=Params2),
	( (public(M1),public(M2)) ; (protected(M1),protected(M2)) ; (private(M1),private(M2))).

	
%-----------------------------------------------------------------------------
%
% Type and SubType Stuff
%
%-----------------------------------------------------------------------------
% extracts a readable type information from type(basic|class, name, num)
generateType(Types,Result) :- 
	member(Type,Types),
	getTypeName(Type,Result).
	
getTypeName(TypeInfo,Result) :- var(Result),TypeInfo=type(A,B,C),
	((A=basic) -> (Temp=B) ; (classDefT(B,_,Temp,_))),
	((C=0) -> (Result=Temp) ; concat_atom([Temp,'[]'],Result) ).
getTypeName(TypeInfo,Result) :- var(TypeInfo),not( Result='' ),
	(concat_atom([Temp,'[]'],Result) -> 
		(classDefT(TypeInfo,_,Temp,_) ; TypeInfo=(basic,Temp,1)); 
		(classDefT(TypeInfo,_,Result,_); TypeInfo=(basic,Result,0))).

% subType stuff
subType(T,SubT) :- extendsT(SubT,T);implementsT(SubT,T);anonymousClass(SubT,T).

subTypePlus(T,SubT) :- var(SubT) -> 
	(subType(T,Mid),subTypePlus(Mid,SubT)) ;
	(subType(Mid,SubT),subTypePlus(T,Mid)).
subTypePlus(T,SubT) :- subType(T,SubT).

subTypeStar(X,SubT) :- subTypePlus(X,SubT).
subTypeStar(X,X).

typeJQ(C) :- classDefT(C,_,_,_) ; primitive(C).

type(X,Y) :- var(X) ->
	( methodDefT(X,_,_,_,type(_,Y,_),_,_);
		fieldDefT(X,_,type(_,Y,_),_,_);
		paramDefT(X,_,type(_,Y,_),_);
		annotationMemberT(X,_,type(_,Y,_),_,_);
		literalT(X,_,_,type(_,Y,_),_);
		localDefT(X,_,_,type(_,Y,_),_,_);
		newArrayT(X,_,_,_,_, type(_,Y,_)); 
		typeCastT(X,_,_,type(_,Y,_),_);
		typeTestT(X,_,_,type(_,Y,_),_) ) ;
	( (getType(X,type(_,Y,_)),!) ; (getType(X,Y)) ).
