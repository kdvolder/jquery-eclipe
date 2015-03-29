%
% Support for browsing SliceJ feature annotations.
%

% For now we just put everything in one file but we may decide later
% to split this up in a similar way to the standard JQuery menus 
% and predicates.
%
% This builds on Java 1.5 annotation support in JQuery


% /////// Predicates ////////////////

%feature(?F,?CodeElement) :- annotation(?CodeElement,?A),name(?A,Feature),attribute(?A,names,?F),String(?F).
%featureName(X) :- findall(FName,(memberValueT(_,_,_,W),newArrayT(W,_,_,_,Ll,_),member(F,Ll),label(F,FName),FL),member(FL,X).
feature(F,CodeElement) :- annotationT(_,_,CodeElement,Type,Xl),label(Type,'Feature'),member(X,Xl),memberValueT(X,_,_,W),newArrayT(W,_,_,_,Ll,_),member(F,Ll).

%feature("./NONE",?X) :- ( constructor(?X); method(?X); field(?X); type(?X) ),NOT(EXISTS ?A:annotation(?X,?A),name(?A,Feature)).
%noneFeature(X) :- annotationT(_,_,_,Type,Xl),label(Type,'Feature'),member(X,Xl),memberValueT(X,_,_,W),newArrayT(W,_,_,_,Ll,_),member(X,Ll),label(X,'./NONE').

%noneFeature(X) :- label(X,'./NONE').
%feature(F,CE) :- noneFeature(F),( constructor(CE);method(CE);field(CE);type(CE) ),not( (annotationT(_,_,CE,Type,_),label(Type,'Feature')) ).

pasteSnippet('feature(F,CodeElement)','CodeElement belongs to feature F').

%baseImage(ImplementsClause<?,?>, DESC_OBJS_INTERFACE).

%label(ImplementsClause<?C,?I>,{class ?Cname implements ?Iname}) :-name(?C,?Cname), name(?I,?Iname).
	
% //sourceLocation(ImplementsClause<?C,?I>,?loc) :- sourceLocation(?C,?loc).

% //child(ImplementsClause<?C,?>,?C).

%feature(?F) :- feature(?F,?).

%feature(?F,ImplementsClause<?C,?I>) :- implements(?C,?I), String(?F),annotation(?C,?A), name(?A, Implement), annotation(?A,?M),attribute(?M,"key",?F),attribute(?M,"values",?Iname),name(?I,?Iname).

pasteSnippet("feature(?F)","?F is the name of a feature occurring in the codebase").

%export(?CodeElement, ?To) :-annotation(?CodeElement,?A),name(?A,Export),attribute(?A,to,?To),String(?To).

pasteSnippet("export(?CodeElement,?To)","The signature of ?CodeElement is exported to feature ?To").

%depends(?X,?Y,?Loc) :- calls(?X,?Y,?Loc);accesses(?X,?Y,?Loc);overrides(?X,?Y),sourceLocation(?X,?Loc).

%neededExport(?E,?Target,?Loc) :- export(?E,?Target),
%	// exporting to one's self is not needed: 
%	NOT(feature(?Target,?E)),
%	// something in target feature should depend on exported element:
%	depends(?x,?E,?Loc), 
%	feature(?Target,?x).

% /////// Toplevel menus ////////////

topQuery("Features","feature(?F)",["?F"]).

% /////// Sub menus /////////////////

%menuItem(?this,["Contents"],"feature(?this,?E),simplePathTo(?E,?P)", ["?P"]) :-feature(?this).

%menuItem(?this,["Exports"],"feature(?this,?E),export(?E,?to),simplePathTo(?E,?P)", ["?P","?to"]) :-feature(?this).
    
%menuItem(?this,["Unneeded Exports"],"feature(?this,?E),export(?E,?to),NOT(neededExport(?E,?to,?)),simplePathTo(?E,?P)", ["?P","?to"]) :-feature(?this).

%//menuItem(?this,[{Exports to ?to}],{feature(??this,??E),export(??E,"?to"),simplePathTo(??E,??P)}, ["?P"]) :-
%//    feature(?this,?E),export(?E,?to).
    
%menuItem(?this,["Imports"],"export(?E,?this),simplePathTo(?E,?P)", ["?P"]) :-feature(?this).
    
%menuItem(?this,["Needed Imports"],"export(?E,?this),neededExport(?E,?this,?Loc),simplePathTo(?E,?P)",["?P","?Loc"]) :- feature(?this).

%menuItem(?this,["Unneeded Imports"],"export(?E,?this),NOT(neededExport(?E,?this,?)),simplePathTo(?E,?P)",["?P"]) :- feature(?this).
    
% /////// Icons /////////////////////
%baseImage(?X,"Feature.gif") :- feature(?X).