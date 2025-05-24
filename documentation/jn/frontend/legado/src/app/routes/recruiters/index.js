
export default {
    path: 'recrutadores',
    indexRoute: {
    },
    childRoutes: [
         require('./searchCandidates').default,
		 require('./vagas').default

    ]
};