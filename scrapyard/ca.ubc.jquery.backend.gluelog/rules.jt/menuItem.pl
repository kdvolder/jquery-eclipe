%
% This user-configurable file defines rules and menuItems that JQuery
% JQuery uses to populate the context menus (pop-up menus).
% etc.

% --------------------------------------------------------------------
% The new form for a menuItem is as follows:
%
%    menuItem(This, LabelString, QueryString, [VarsString0, VarsString1, ...]) :- ApplicabilityExp).
% --------------------------------------------------------------------

% Helper to make dynamic menu queries
generateMenuQuery(Name,Query,Result) :- 
	string_to_atom(NameString,Name),string_concat('Name=',NameString,TempA),
	string_concat(TempA,',',TempB),string_concat(TempB,Query,Result).

% --------------------------------------------------------------------
% Members->Constructors
% --------------------------------------------------------------------
menuItem(This, ['Members','Constructors'], 'child(This,M),method(M),constructor(M)', ['M']) :- 
	class(This).

% --------------------------------------------------------------------
% Members->Methods
% --------------------------------------------------------------------
menuItem(This, ['Members','Methods'], 'child(This,M),method(M),not(constructor(M))', ['M']) :- 
	class(This);interface(This).

%menuItem(This, ['Members','Methods...', Name], Q, ['M']) :- 
%	classDefT(This,_,_,_),methodDefT(Mid,This,Name,_,_,_,_),not(constructor(Mid)),
%	generateMenuQuery(Name,'method(M),child(This,M),methodName(M,Name),not(constructor(M))',Q).

% --------------------------------------------------------------------
% Members->Fields
% --------------------------------------------------------------------
menuItem(This, ['Members','Fields'], 'child(This,F),field(F)', ['F']) :- 
	class(This);enum(This).

% --------------------------------------------------------------------
% Members->Initializers
% --------------------------------------------------------------------
menuItem(This, ['Members','Initializers'] , 'child(This,I),initializer(I)', ['I']) :- 
	class(This).

% --------------------------------------------------------------------
% Members->Outline
% --------------------------------------------------------------------
outlineView(X,[C|CV]) :- child(X,C),outlineView(C,CV).
outlineView(X,[]) :- class(X);enum(X);interface(X);method(X);field(X).

menuItem(This, ['Members','Outline'], 'outlineView(This,X)', ['X']) :-
	typeJQ(This).

% --------------------------------------------------------------------
% Signature->Throws
% --------------------------------------------------------------------

menuItem(This, ['Signature','Throws'] , 'throws(This,E)', ['E']) :- 
	method(This).

% --------------------------------------------------------------------
% Signature->Returns
% --------------------------------------------------------------------

menuItem(This, ['Signature','Returns'] , 'methodDefT(This,_,_,_,Re,_,_),type(Re,R)', ['R']) :- 
	method(This).

% --------------------------------------------------------------------
% Signature->Arguments
% --------------------------------------------------------------------

menuItem(This, ['Signature','Arguments'] , 'param(This,P)', ['P']) :- 
	method(This).

% --------------------------------------------------------------------
% Signature->Modifiers
% --------------------------------------------------------------------

menuItem(This, ['Signature','Modifiers'] , 'modifierT(This, Mod)', ['Mod']) :- 
	method(This).

% --------------------------------------------------------------------
% Signature->Signature
% --------------------------------------------------------------------

menuItem(This, ['Signature','Signature'] , 'methodSignature(This,Sig)', ['Sig']) :-
	method(This).

menuItem(This, ['Signature', 'Methods Like This'], 'likeThis(This,M),child(C,M)', ['C','M']) :-
	method(This).

% --------------------------------------------------------------------
% References Methods
% --------------------------------------------------------------------
menuItem(This, 
	['Calls','Outgoing Calls'], 
	'calls(This,M,Ref),child(C,M)',
	['C','M','Ref']) :- 
		method(This).

%menuItem(This, ['Calls','Outgoing Calls...',Name], Q, ['C','M','Ref']) :- 
%		method(This),calls(This,M,_),methodName(M,Name),
%		generateMenuQuery(Name,'calls(This,M,Ref),child(C,M),methodName(M,Name)',Q).

menuItem(This, 
	['Calls','Incoming Calls'], 
	'((This=T);overrides(T,This)),polyCalls(M,T,Ref),child(C,M)',
	['C','M','Ref']) :- 
		method(This).

%menuItem(This, ['Calls','Incoming Calls...',Name], Q, ['C','M','Ref']) :- 
%		method(This),polyCalls(M,This,_),methodName(M,Name),
%		generateMenuQuery(Name,'polyCalls(M,This,Ref),child(C,M),methodName(M,Name)',Q).

menuItem(This,
	['Calls', 'Creates'],
	'creator(C,This,Ref),package(P,C)',
	['P','C','Ref']) :-
		method(This).


% --------------------------------------------------------------------
% Subclasses
% --------------------------------------------------------------------

menuItem(This, ['Inheritance','Subclasses'] , 'subType(This,S)', ['S']) :-
	class(This).

% --------------------------------------------------------------------
% Superclass
% --------------------------------------------------------------------

menuItem(This, ['Inheritance','Superclass'], 'subType(S,This)', ['S']) :-
	class(This).

% --------------------------------------------------------------------
% Implements
% --------------------------------------------------------------------

menuItem(This, ['Inheritance','Implements Interfaces'] , 'subType(This,I),interface(I)', ['I']) :-
	class(This).

% --------------------------------------------------------------------
% Implemented by
% --------------------------------------------------------------------

menuItem(This, ['Inheritance','Implemented by'] , 'subType(C,This)', ['C']) :-
	interface(This).

% --------------------------------------------------------------------
% Subinterface
% --------------------------------------------------------------------

menuItem(This, ['Inheritance', 'Subinterfaces'] ,  'subType(S,This),interface(S)', ['S']) :-
	interface(This).

% --------------------------------------------------------------------
% Superinterfaces
% --------------------------------------------------------------------

menuItem(This, ['Inheritance','Super-Interfaces'] , 'subType(This,S),interface(S)', ['S']) :-
	interface(This).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

menuItem(This, ['Inheritance','Subtypes+'], 'subTypePlus(This,SubT)', ['SubT']) :-
	classDefT(This,_,_,_).

menuItem(This, ['Inheritance','Supertypes+'], 'subTypePlus(SuperT,This)', ['SuperT']) :-
	classDefT(This,_,_,_).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


menuItem(This, ['Inheritance','Inherited Fields'], 'inheritedField(This,F,Sup)', ['Sup','F']) :-
	classDefT(This,_,_,_).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

menuItem(This, ['Inheritance','Inherited Methods'], 'inheritedMethod(This,M,Super)', ['Super','M']) :-
	classDefT(This,_,_,_).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
menuItem(This, ['Inheritance','Hierarchy'], 'hierarchy(This,H),not(H==[])', ['H']) :-
	reference(This).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
menuItem(This, ['Inheritance','Inverted Hierarchy'], 'inv_hierarchy(This,IH),not(IH==[])', ['IH']) :-
	reference(This).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

menuItem(This,['Inheritance','Overridden by'],'overrides(Rides,This),package(P,C),child(C,Rides),method(Rides)', ['P','C','Rides']) :-
	inheritableMethod(This).

menuItem(This, ['Inheritance','Override'], 'overrides(This,Rides),package(P,C),child(C,Rides),method(Rides)', ['P','C','Rides']) :-
	inheritableMethod(This).

menuItem(This,['Inheritance', 'Inherited by'],
	'child(ThisC,This),subTypePlus(ThisC,C)', ['C']) :-
	inheritableMethod(This).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% --------------------------------------------------------------------
% Package -> Types
% --------------------------------------------------------------------

menuItem(This, ['Top-level Types'],'package(This,T),typeJQ(T)', ['T']) :-
	package(This).

menuItem(This, ['Types...',Name],Q,['T']):-
	package(This),classDefT(_,This,Name,_),generateMenuQuery(Name,'classDefT(T,This,Name,_)',Q).

% --------------------------------------------------------------------
% Package -> Classes
% --------------------------------------------------------------------

menuItem(This, ['Top-level Classes'] , 'package(This,T),class(T)', ['T']) :-
	package(This).

% --------------------------------------------------------------------
% Package -> Interfaces
% --------------------------------------------------------------------

menuItem(This, ['Top-level Interfaces'] , 'package(This,T),interface(T)', ['T']) :-
	package(This).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%/

menuItem(This,['Java Structure','Contained In'],'child(P,This)',['P']) :-
	not( package(This) ).

menuItem(This,['Java Structure', 'Contains'],'child(This,C)',['C']) :- 
	not( primitive(This) ).

menuItem(This,['Java Structure','My Structure'],'viewFromHere(This,View),not(View==[])',['View']) :- 
	not( primitive(This) ).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%/

menuItem(This,
	['Creators'],
	'subTypePlus(This,Cted),creator(Cted,CtorM,Ref),child(CtorCls,CtorM),package(CtorP,CtorCls)',
	['CtorP', 'CtorCls','CtorM', 'Ref']) :-
	interface(This) ; abstractClass(This).

menuItem(This,
	['Creators'],
	'creator(This,CtorM,Ref),child(Cted,CtorM),package(CtedP,Cted)',
	['CtedP', 'Cted', 'CtorM', 'Ref']) :-
    class(This),not( abstract(This) ).

menuItem(This, ['Subtype Creators'],
	'subTypePlus(This,Cted),creator(Cted,CtorM,Ref),child(CtorCls,CtorM),package(CtorP,CtorCls)',
	['Cted','CtorP','CtorCls','CtorM','Ref']) :-
	reference(This).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%/

menuItem(This,['Type of Field'],'type(This,T)',['T']) :-
	field(This).

menuItem(This,['Field Accesses','Reads/Writes Fields'],'accesses(This,F,Ref),child(T,F)',['T','F','Ref']) :- 
	method(This).
menuItem(This,['Field Accesses','Writes Fields'],'writes(This,F,Ref),child(T,F)',['T','F','Ref']) :- 
	method(This).
menuItem(This,['Field Accesses','Reads Fields'],'reads(This,F,Ref),child(T,F)',['T','F','Ref']) :- 
	method(This).

menuItem(This,['Read/Written by'],'accesses(M,This,Ref),method(M),child(C,M)',['C','M','Ref']) :- 
	field(This).
menuItem(This,['Written by'],'writes(M,This,Ref),method(M),child(C,M)',['C','M','Ref']) :- 
	field(This).
menuItem(This,['Read by'],'reads(M,This,Ref),method(M),child(C,M)',['C','M','Ref']) :- 
	field(This).

% Notes to Ryan... I started looking at the above because they didn't work.
%   a)  You wrote: including, for classes, reads occuring in child methods
%      Kris >  not good enough, there could also be reads in other places than the methods!
%   b) If you want to make it work... why not make it a separate rule / query for classes (somewhat) like this:
%       menuItem(?this, ['Field Accesses', 'Reads Fields'], 'child(?this,?m),reads(?m,?f,?Ref),child(?T,?f)', ['?T','?f','?Ref']) :- class(?this).
%    There is no need to try and twist the query for fields into the one for classes.
%    It's really not necessary that because they have the same menu label they should also have the same query.


%%%%%%%%%%%%%%%%%%
%Code Marker Sub-Queries
%%%%%%%%%%%%%%%%%%
%menuItem(?this, ['Markers', 'Compiler Warnings'],'child+(!this,?W),warning(?W)', ['?W']) :- Element(?this),NOT(Marker(?this)).
%menuItem(?this, ['Markers', 'Compiler Errors'],'child+(!this,?W),error(?W)', ['?W']) :- Element(?this),NOT(Marker(?this)).
%menuItem(?this, ['Markers', 'Bookmarks'],'child+(!this,?B),bookmark(?B)', ['?B']) :- Element(?this),NOT(Marker(?this)).
%menuItem(?this, ['Markers', 'Tasks'],'child+(!this,?B),task(?B),priority(?B,?P)', ['?P','?B']) :- Element(?this),NOT(Marker(?this)).
%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%/
% METHOD HIERARCHY
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%/
% [previously] Disabled due to tyruba issue with intersection of environments.
%  see tyRuBa.tests.TypeTest.java:testUserDefinedListType

%methodizeHierarchy(?sig,[],[]) :- String(?sig).

%methodizeHierarchy(?sig,[?C1|?CH],?mH) :-
%   Type(?C1),
%   NOT( EXISTS ?m : method(?C1,?m), signature(?m,?sig) ),
%   methodizeHierarchy(?sig,?CH,?mH).

%methodizeHierarchy(?sig,[?C1|?CH],[?C1,?m|?mH]) :-
%   method(?C1,?m), signature(?m,?sig),
%   methodizeHierarchy(?sig,?CH,?mH).

%methodHierarchy(?m,?mH) :- inheritableMethod(?m),
%   child(?C,?m), type(?C),
%   signature(?m,?sig),hierarchy(?C, ?CH),
%   methodizeHierarchy(?sig,?CH,?mH).

%menuItem(?this,['Inheritance', 'Method Hierarchy'],
%'methodHierarchy(!this,?H)', ['?H']) :-
%    inheritableMethod(?this),child(?T,?this),
%    name(?this,?name),name(?T,?Tname).

%-----------------------------------------------------------------------------
%	Usage
%-----------------------------------------------------------------------------

menuItem(This,['Usage', 'Field with type'],
	'type(F,This),field(F),child(C,F)', ['C','F']) :-
	typeJQ(This).

menuItem(This,['Usage', 'Instance of test'],
	'instanceOf(Caller,This,Ref),child(C,Caller)', ['C','Caller','Ref']) :- 
	reference(This).

menuItem(This,['Usage', 'Argument'],
	'param(Method,This),child(C,Method)', ['C','Method']) :- 
	typeJQ(This).

menuItem(This,['Usage', 'Return type'],
	'type(M,This),method(M),child(C,M)', ['C','M']) :-
    typeJQ(This).

menuItem(This,['Usage','Catches'],
	'catches(M,This,Ref),child(C,M)',['C','M','Ref']) :-
	throws(_,This).    