%-----------------------------------------------------------------------------
%
% Simple Tests
%
%-----------------------------------------------------------------------------

%-----------------------------------------------------------------------------
%	Defined Simple Predicates
%-----------------------------------------------------------------------------
simplePredicate(X,Y) :- simplePredicateJQ(Y,X).

simplePredicateJQ('typeJQ(X)','X is a class, interface, or primitive').
simplePredicateJQ('type(X,Y)','Y is the type of X').
simplePredicateJQ('primitive(X)','X is primitive (int, float, etc.)').
simplePredicateJQ('reference(X)','X is a reference type').
simplePredicateJQ('child(X,Y)','Y is a child of X.  Hierarchy is Package->File->Class->Member').

simplePredicateJQ('abstract(X)','X is abstract').
simplePredicateJQ('static(X)','X is static').
simplePredicateJQ('public(X)','X is public').
simplePredicateJQ('protected(X)','x is protected').
simplePredicateJQ('private(X)','X is private').

simplePredicateJQ('package(X)','X is a package').
simplePredicateJQ('package(P,C)','C is a class/interface contained in P').

simplePredicateJQ('class(X)','X is a class').
simplePredicateJQ('enum(X)','X is an enum').
simplePredicateJQ('anonymousClass(X,C)','X is an anonymousClass which extends type C').
simplePredicateJQ('interface(X)','X is an interface').
simplePredicateJQ('hierarchy(C1,C2[])','C2 is below C1 in the class hierarchy').
simplePredicateJQ('inv_hierarchy(C1,C2[])','C2 is above C1 in the class hierarchy').
simplePredicateJQ('viewFromhere(X,C[])','C is a child of X or a child of X\'s child').
simplePredicateJQ('creator(X,M,Loc)','M creates an instance of X at location Loc').

simplePredicateJQ('field(X)','X is a field').
simplePredicateJQ('reads(M,F,Loc)','F is read by method M at location Loc').
simplePredicateJQ('writes(M,F,Loc)','F is written by method M at location Loc').
simplePredicateJQ('accesses(M,F,Loc)','F is read or written by method M at location Loc').
simplePredicateJQ('inheritedField(C,F,Super)','F is a field of C inherited from Super').

simplePredicateJQ('method(X)','X is a method').
simplePredicateJQ('initializer(X)','X is an initializer').
simplePredicateJQ('constructor(X)','X is a constructor').
simplePredicateJQ('methodName(X,N)','N is the name of method X').
simplePredicateJQ('likeThis(M1,M2)','M2 is a method with the same name as M1').
simplePredicateJQ('throws(M,E)','E is an exception thrown by M').
simplePredicateJQ('param(M,P)','P is a parameter of M').
simplePredicateJQ('calls(Caller,Callee,Loc)','Caller makes a call to Callee at location Loc').
simplePredicateJQ('polyCalls(Caller,Callee,Loc)','Caller makes a call to Callee or method overriden by Callee at location Loc').
simplePredicateJQ('nonAbstractMethod(M)','M is a non abstract method').
simplePredicateJQ('inheritableMethod(M)','M is inheritable').
simplePredicateJQ('inheritedMethod(C,M,Super)','Method M from class C is inherited from class Super').

simplePredicateJQ('instanceOf(X,Y,Loc)','(X instanceof Y) at location Loc').

%compilationUnit(X) :- fileT(X,_,_,_).
%fileName(X,N) :- fileT(X,_,T,_),parseFileName(T,N).
%filePath(X,N) :- fileT(X,_,N,_).
%hasSource(C) :- sourceLocation(C,_,_,_).
%javaFile(N) :- parseFileExtension(N,'java').
%classFile(N) :- parseFileExtension(N,'class').


%-----------------------------------------------------------------------------
% Modifier
%-----------------------------------------------------------------------------
abstract(X) :- modifierT(X,'abstract').
static(X) :- modifierT(X,'static').
public(X) :- modifierT(X,'public').
protected(X) :- modifierT(X,'protected').
private(X) :- modifierT(X,'private').


%-----------------------------------------------------------------------------
% Package
%-----------------------------------------------------------------------------
package(X) :- packageT(X,_),not(X==null).
package(P,C) :- classDefT(C,P,_,_),package(P).

%-----------------------------------------------------------------------------
% File
%-----------------------------------------------------------------------------
compilationUnit(X) :- fileT(X,_,_,_).
fileName(X,N) :- fileT(X,_,T,_),parseFileName(T,N).
filePath(X,N) :- fileT(X,_,N,_).
hasSource(C) :- sourceLocation(C,_,_,_).

javaFile(N) :- parseFileExtension(N,'java').
classFile(N) :- parseFileExtension(N,'class').


%-----------------------------------------------------------------------------
% Class
%-----------------------------------------------------------------------------
class(X) :- classDefT(X,_,_,_),not( interfaceT(X);enumT(X) ).
enum(X) :- classDefT(X,_,_,_),enumT(X).
interface(X) :- interfaceT(X),classDefT(X,_,_,_).
concreteClass(X) :- class(X),not( modifierT(X,'abstract') ).
abstractClass(X) :- class(X),modifierT(X,'abstract').
anonymousClass(X,C) :- classDefT(X,NewP,_,_),newClassT(NewP,_,_,_,_,P,_,_),identT(P,_,_,_,C).

hierarchy(C1,[]) :- typeJQ(C1),not( subType(C1,_) ).
hierarchy(C1,[C2|R]) :- subType(C1,C2),hierarchy(C2,R).

inv_hierarchy(T,[]) :- not( subType(_,T) ).
inv_hierarchy(Sub,[Super|R]) :- subType(Super,Sub),inv_hierarchy(Super,R).

inv_type_hierarchy(T,[]) :- not( subType(_,T) ).
inv_type_hierarchy(Sub,[Super|R]) :- extendsT(Sub,Super),inv_type_hierarchy(Super,R).

hierarchyView(X,Y) :- 
	hierarchy(X,HL),inv_type_hierarchy(X,IHLr),reverse(IHLr,IHL),append([X],HL,T),append(IHL,T,Y).

viewFromHere(X,[Child | ChildsView]) :- child(X,Child),viewFromHere(Child,ChildsView).
viewFromHere(X, []) :- not( child(X,_) ).
				
% normal
creator(This,CtorM,Ref) :- methodDefT(Cons,This,_,_,_,_,_),newClassT(Ref,_,CtorM,Cons,_,_,_,_).
% anonymous class
creator(This,CtorM,Ref) :- newClassT(Ref,_,CtorM,_,_,_,C,_),anonymousClass(C,This).


%-----------------------------------------------------------------------------
% Field
%-----------------------------------------------------------------------------
field(X) :- fieldDefT(X,_,_,_,_);enumConstantT(X,_,_,_,_).

% Field reads/writes
reads(Mid,Field,Ref) :- getFieldT(Ref,P,Mid,_,_,Field),not( (assignopT(P,_,_,_,_,_);assignT(P,_,_,_,_)) ).
writes(Mid,Field,Ref) :- getFieldT(Ref,P,Mid,_,_,Field),( (assignopT(P,_,_,_,_,_);assignT(P,_,_,_,_)) ).
accesses(Mid,Field,Ref) :- getFieldT(Ref,_,Mid,_,_,Field).

inheritedField(C,F,Super) :- subTypePlus(Super,C),fieldDefT(F,Super,_,_,_),not( modifierT(F,'private') ).


%-----------------------------------------------------------------------------
% Method
%-----------------------------------------------------------------------------
method(X) :- methodDefT(X,_,_,_,_,_,_).
initializer(X) :- methodDefT(X,_,'<clinit>',_,_,_,_).
constructor(X) :- methodDefT(X,_,'<init>',_,_,_,_).

% Signature stuff
methodName(M,N) :- methodDefT(M,_,N,_,_,_,_).
likeThis(M1,M2) :- methodName(M1,N),methodName(M2,N),not( M1=M2 ).
throws(M,E) :- methodDefT(M,_,_,_,_,EL,_),member(E,EL).
param(Method,P) :- nonvar(P) -> (paramDefT(_,Method,type(_,P,_),_)) ; (paramDefT(_,Method,PL,_),type(PL,P)).
catches(M,E,Ref) :- catchT(A,_,M,_,_),paramDefT(Ref,A,type(_,E,_),_).

% calls
calls(Caller,Callee,Ref) :- callT(Ref,_,Caller,_,_,_,Callee).
calls(Caller,Callee,Ref) :- constructor(Callee),newClassT(Ref,_,Caller,Callee,_,_,_,_).
polyCalls(Caller,Callee,Ref) :- calls(Caller,Callee,Ref).
polyCalls(Caller,Callee,Ref) :- calls(Caller,M,Ref),overrides(Callee,M).

% Helper for overrides/implements adornments
nonAbstractMethod(M) :- method(M),not( abstract(M) ),child(P,M),not( interface(P) ).

inheritableMethod(M) :- 
	methodDefT(M,_,_,_,_,_,_),not( (constructor(M);modifierT(M,'private');modifierT(M,'static')) ).

% Has Method
hasMethod(C,M,C) :- methodDefT(M,C,_,_,_,_,_).
hasMethod(C,M,From) :- 
	subType(Super,C),hasMethod(Super,M,From),inheritableMethod(M),
	not( (strongLikeThis(M,MyOwn),child(C,MyOwn)) ).
	
% M overrides MSuper
overrides(M,MSuper) :-
	strongLikeThis(M,MSuper),inheritableMethod(MSuper),
	child(C,M),child(Super,MSuper),
	subTypePlus(Super,C).
	
inheritedMethod(C,M,Super) :- hasMethod(C,M,Super),not(C==Super).


%-----------------------------------------------------------------------------
% Usage
%-----------------------------------------------------------------------------
instanceOf(Target,Test,Ref) :- typeTestT(Ref,_,Target,type(_,Test,_),_).


%-----------------------------------------------------------------------------
% Misc...
%-----------------------------------------------------------------------------
primitive(C) :- C=type(Kind,_,_),Kind=basic.
reference(C) :- classDefT(C,_,_,_).


% Y is a child of X
% We have something like: 
%	package 
%		file
%			class
%				innerclass
%				method
%					statement (call, set, read, etc.) <- would be nice but too slow :(
%				field
child(X,Y) :- 
	(methodDefT(Y,X,_,_,_,_,_));
	(fieldDefT(Y,X,_,_,_));
	(enumConstantT(Y,X,_,_,_));
	(package(X),fileT(Y,X,N,_),sourceLocation(Y,N,_,_));
	(fileT(X,P,N,_),classDefT(Y,P,_,_),sourceLocation(Y,N,_,_));
	(typeJQ(X),classDefT(Y,X,_,_));
	% check for anonymous classes
	(method(X),classDefT(Y,T,_,_),newClassT(T,_,X,_,_,_,_,_)).
	% check for any statement with parent as method (but it's too slow!!)
%	(method(X),enclMethod(Y,X)).
