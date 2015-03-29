% =====================================================================
% Top Level Queries
% =====================================================================

topQuery(['Features'],
	'annotationT(A,_,Z,Type,Xl),child(C,Z),member(X,Xl),memberValueT(X,_,_,W),newArrayT(W,_,_,_,Ll,_),member(L,Ll)',
	['Type','C','Z','L']).

topQuery(['Annotations'],'annotationT(A,Target,_,Type,_),memberValueT(_,A,_,Loc)',['Target','Type','Loc']).

topQuery(['Package Browser (.java files)'],'package(P,C),hasSource(P)',['P','C']).

topQuery(['Package Browser (.java|.class files)'],'package(P,T),typeJQ(T)',['P','T']).

topQuery(['Class Creation'],
	'class(Cted),package(CtedP,Cted),creator(Cted,CtorM,Ref),child(CtorC,CtorM),package(CtorP,CtorC)',
	['CtedP','Cted','CtorP','CtorC','CtorM','Ref']).

topQuery(['Interface Implementation'],'interface(Itf),hierarchy(Itf,H),(hasSource(Itf);hasSource(H))',['Itf','H']).		

% An alternative interface implementation browser		
topQuery(['Interface Implementation 2'],
	'interface(Itf),subTypePlus(Itf,Cls),class(Cls),package(CPkg,Cls),package(IPkg,Itf)',['CPkg','Cls','Itf']).

topQuery(['Abstract Classes'],'class(Cls),abstract(Cls),hierarchy(Cls,H),hasSource(Cls)',['Cls','H']).
		
topQuery(['InstanceOf Testing'],
	'instanceOf(Tester,Tested,Loc),package(P,Tested),child(Ctester,Tester),package(Ptester,Ctester)',
	['P','Tested','Ptester','Ctester','Tester','Loc']).

% Experimental:Structure Browser: Displays the full java structure of the
% code, including outer and nested (local, anonymous, member, static nested)
% types as well as fields and methods of each, sorted by child order starting
% with packages.  
topQuery(['Java Structure Browser'],'package(Pkg),viewFromHere(Pkg,View),hasSource(Pkg)',['Pkg','View']).
		
topQuery(['Method Browser'],'package(P,C),child(C,M),method(M),hasSource(M)',['P','C','M']).

topQuery(
	['Abstract Method Browser'],
	'(interface(T);abstract(M)),child(T,M),hasSource(M),method(M),overrides(SubM,M),child(SubC,SubM)',
	['T','M','SubC','SubM']).
 
% FIXME topQuery('Tags',{tag(??X,??Tag,??Val),pathTo(??X,??path)}, [ '?Tag', '?Val', '?path' ]).

% FIXME topQuery('Deprecated Elements','deprecatedComment(?E,?comment),pathTo(?E,?Path)',['?Path','?comment']).
   
% FIXME topQuery('Deprecated Uses','deprecatedComment(?E,?comment),polyCalls(?X,?E,?loc),pathTo(?X,?Path)',['?Path','?loc','?comment']).
   
% Compiler Error and Warning queries
% FIXME topQuery('Compiler Warnings', 'warning(?W),child(?E,?W)',['?W','?E']).
% FIXME topQuery('Compiler Errors', 'error(?W),child(?E,?W)',['?W','?E']).
% FIXME topQuery('Bookmarks','bookmark(?B)',['?B']).
% FIXME topQuery('Tasks','task(?T),child(?E,?T),priority(?T,?Pr)',['?Pr','?T','?E']).

% FIXME topQuery('JUnit tests',{subtype+(?T,??Test),class(??Test),NOT(modifier(??Test,abstract)),method(??Test,??test),re_name(??test,/^test/),package(??Test,??P)},['?P','?Test','?test']):- name(?T,TestCase),Type(?T).