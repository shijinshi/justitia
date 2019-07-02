export default [
  // user
  {
    path: '/user',
    component: '../layouts/UserLayout',
    routes: [
      { path: '/user', redirect: '/user/login' },
      { path: '/user/login', component: './User/Login' },
      { path: '/user/register', component: './User/Register' },
      { path: '/user/password', component: './User/Password' },
      { path: '/user/register-result', component: './User/RegisterResult' },
    ],
  },
  // app
  {
    path: '/',
    component: '../layouts/BasicLayout',
    Routes: ['src/pages/Authorized'],
    authority: ['admin', 'non-root', 'root'],
    routes: [
      //fabricCA
      { path: '/', redirect: '/fabricCA' },
      {
        name: 'fabricCA',
        path: '/fabricCA',
        icon: 'form',
        routes: [
          { path: '/fabricCA', redirect: '/fabricCA/CAManager' },
          {
            path: '/fabricCA/CAManager',
            name: 'CAManager',
            component: './FabricCA/CAManager'
          },
          {
            path: '/fabricCA/CAUser',
            name: 'CAUserManager',
            component: './FabricCA/CAUser'
          }
        ]
      },
      //host
      {
        path: '/host',
        name: 'host',
        icon: 'form',
        routes: [
          { path: '/host', redirect: '/host/manager' },
          {
            path: '/host/manager',
            name: 'host',
            component: './Host/hostManager'
          }, {
            path: '/host/docker/list',
            name: 'dockerManager',
            component: './Host/dockerList'
          },
          {
            path: '/host/docker/imageManager',
            name: 'imageManager',
            component: './Host/dockerImage'
          },
          {
            path: '/host/docker/networkManager',
            name: 'networkManager',
            component: './Host/dockerNet'
          },
          {
            path: '/host/docker/volumeManager',
            name: 'volumeManager',
            component: './Host/dockerVolume'
          }
        ]
      },
      // network
      {
        path: '/network',
        name: 'network',
        icon: 'form',
        routes: [
          { path: '/network', redirect: '/network/peer' },
          {
            path: '/network/peer',
            name: 'peer',
            icon: 'form',
            component: './Network/peer/peer'
          },
          {
            path: '/network/orderer',
            name: 'orderer',
            icon: 'form',
            component: './Network/orderer/orderer'
          },

        ]
      },
      // channel
      {
        name: 'channel',
        path: "/channel",
        icon: 'form',
        routes: [
          { path: '/channel', redirect: '/channel/manager' },
          {
            path: "/channel/manager",
            name: "ChannelManager",
            component: "./Channel/ChannelManager"
          },
          {
            path: "/channel/task",
            name: "ChannelTask",
            component: "./Channel/ChannelTask"
          },
        ]
      },
      //account
      {
        name: 'account',
        icon: 'user',
        path: '/account',
        routes: [
          {
            path: '/account/settings',
            name: 'settings',
            component: './Account/Settings/Info',
            routes: [
              {
                path: '/account/settings',
                redirect: '/account/settings/base',
              },
              {
                path: '/account/settings/base',
                component: './Account/Settings/BaseView',
              },
              {
                path: '/account/settings/binding',
                component: './Account/Settings/BindingView',
              },
            ],
          },
        ],
      },
      // exception
      {
        // name: 'exception',
        // icon: 'warning',
        path: '/exception',
        routes: [
          // exception
          {
            path: '/exception/403',
            // name: 'not-permission',
            component: './Exception/403',
          },
          {
            path: '/exception/404',
            // name: 'not-find',
            component: './Exception/404',
          },
          {
            path: '/exception/500',
            // name: 'server-error',
            component: './Exception/500',
          },
          {
            path: '/exception/trigger',
            // name: 'trigger',
            hideInMenu: true,
            component: './Exception/triggerException',
          },
        ],
      },
      {
        component: '404',
      },
    ],
  },
];
