%
% Update target filters
%
updateTargetFilter('Types','typeJQ(This)').
updateTargetFilter('Top level types','package(_,This)').
updateTargetFilter('Methods','method(This)').
updateTargetFilter('Members','method(This);field(This)').
updateTargetFilter('Fields','field(This)').
updateTargetFilter('Packages','package(This)').


%
% Modifier Filters
%
filterItem(This,['modifier','public'],'public(This)') :- 
    method(This);field(This);reference(This).
    
filterItem(This,['modifier','protected'],'protected(This)') :- 
	method(This);field(This);reference(This).
	
filterItem(This,['modifier','private'],'private(This)') :- 
    method(This);field(This);reference(This).
    
filterItem(This,['modifier','static'],'static(This)') :-
    method(This);field(This);reference(This).
    
filterItem(This,['modifier','abstract'],'abstract(This)') :-
    method(This);reference(This).

% 
% Class Filters
%
filterItem(This,['interface'],'interface(This)') :-
	reference(This).
filterItem(This,['anonymous'],'anonymous(This,Super)') :-
	reference(This).	

%
% Method Filters
%
filterItem(This,['has parameters'],'param(This,P)') :-
	method(This).
	
filterItem(This,['throws exception'],'throws(This,E)') :-
   	method(This).