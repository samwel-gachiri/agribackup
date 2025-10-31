<template>
  <div class="notification-system">
    <!-- Toast Notifications -->
    <div class="toast-container tw-fixed tw-top-4 tw-right-4 tw-z-50 tw-space-y-3">
      <transition-group name="toast" tag="div">
        <div
          v-for="notification in notifications"
          :key="notification.id"
          :class="getToastClasses(notification)"
          class="toast tw-max-w-sm tw-rounded-lg tw-shadow-lg tw-p-4 tw-flex tw-items-start tw-gap-3"
        >
          <!-- Icon -->
          <div class="tw-flex-shrink-0">
            <v-icon :color="getIconColor(notification.type)" size="20">
              {{ getIcon(notification.type) }}
            </v-icon>
          </div>

          <!-- Content -->
          <div class="tw-flex-1 tw-min-w-0">
            <h4 v-if="notification.title" class="tw-font-medium tw-text-sm tw-mb-1">
              {{ notification.title }}
            </h4>
            <p class="tw-text-sm tw-opacity-90">
              {{ notification.message }}
            </p>

            <!-- Actions -->
            <div v-if="notification.actions && notification.actions.length > 0" class="tw-mt-3 tw-flex tw-gap-2">
              <button
                v-for="action in notification.actions"
                :key="action.key"
                @click="handleAction(notification.id, action)"
                class="tw-text-xs tw-font-medium tw-px-3 tw-py-1 tw-rounded tw-border tw-border-current tw-opacity-75 hover:tw-opacity-100 tw-transition-opacity"
              >
                {{ action.label }}
              </button>
            </div>
          </div>

          <!-- Close Button -->
          <button
            v-if="notification.closable !== false"
            @click="removeNotification(notification.id)"
            class="tw-flex-shrink-0 tw-opacity-50 hover:tw-opacity-100 tw-transition-opacity"
          >
            <v-icon size="16">mdi-close</v-icon>
          </button>
        </div>
      </transition-group>
    </div>

    <!-- Banner Notifications -->
    <div v-if="bannerNotification" class="banner-container tw-fixed tw-top-0 tw-left-0 tw-right-0 tw-z-40">
      <div :class="getBannerClasses(bannerNotification)" class="banner tw-p-4">
        <div class="tw-max-w-7xl tw-mx-auto tw-flex tw-items-center tw-justify-between">
          <div class="tw-flex tw-items-center tw-gap-3">
            <v-icon :color="getIconColor(bannerNotification.type)" size="24">
              {{ getIcon(bannerNotification.type) }}
            </v-icon>
            <div>
              <h4 v-if="bannerNotification.title" class="tw-font-medium tw-text-sm">
                {{ bannerNotification.title }}
              </h4>
              <p class="tw-text-sm tw-opacity-90">
                {{ bannerNotification.message }}
              </p>
            </div>
          </div>

          <div class="tw-flex tw-items-center tw-gap-3">
            <!-- Banner Actions -->
            <div v-if="bannerNotification.actions && bannerNotification.actions.length > 0" class="tw-flex tw-gap-2">
              <button
                v-for="action in bannerNotification.actions"
                :key="action.key"
                @click="handleBannerAction(action)"
                class="tw-text-sm tw-font-medium tw-px-4 tw-py-2 tw-rounded tw-border tw-border-current tw-opacity-75 hover:tw-opacity-100 tw-transition-opacity"
              >
                {{ action.label }}
              </button>
            </div>

            <!-- Close Button -->
            <button
              v-if="bannerNotification.closable !== false"
              @click="closeBanner"
              class="tw-opacity-50 hover:tw-opacity-100 tw-transition-opacity"
            >
              <v-icon size="20">mdi-close</v-icon>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Notifications -->
    <v-dialog v-model="modalNotification" max-width="500" persistent>
      <v-card v-if="currentModal" class="tw-rounded-xl">
        <v-card-title class="tw-flex tw-items-center tw-gap-3 tw-pb-2">
          <v-icon :color="getIconColor(currentModal.type)" size="24">
            {{ getIcon(currentModal.type) }}
          </v-icon>
          {{ currentModal.title }}
        </v-card-title>

        <v-card-text class="tw-py-4">
          <p>{{ currentModal.message }}</p>
        </v-card-text>

        <v-card-actions class="tw-px-6 tw-pb-4">
          <v-spacer />
          <v-btn
            v-for="action in currentModal.actions || []"
            :key="action.key"
            :color="action.color || getActionColor(currentModal.type)"
            :variant="action.variant || 'outlined'"
            @click="handleModalAction(action)"
          >
            {{ action.label }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template><sc
ript>
export default {
  name: 'NotificationSystem',
  data() {
    return {
      notifications: [],
      bannerNotification: null,
      modalNotification: false,
      currentModal: null,
      nextId: 1
    }
  },
  mounted() {
    // Listen for global notification events
    this.$root.$on('show-notification', this.showNotification)
    this.$root.$on('show-banner', this.showBanner)
    this.$root.$on('show-modal', this.showModal)
  },
  beforeDestroy() {
    this.$root.$off('show-notification', this.showNotification)
    this.$root.$off('show-banner', this.showBanner)
    this.$root.$off('show-modal', this.showModal)
  },
  methods: {
    showNotification(options) {
      const notification = {
        id: this.nextId++,
        type: options.type || 'info',
        title: options.title,
        message: options.message,
        duration: options.duration || 5000,
        closable: options.closable !== false,
        actions: options.actions || [],
        persistent: options.persistent || false
      }

      this.notifications.push(notification)

      // Auto remove after duration (unless persistent)
      if (!notification.persistent && notification.duration > 0) {
        setTimeout(() => {
          this.removeNotification(notification.id)
        }, notification.duration)
      }
    },
e>
</styl 2px;
}dius:der-ra
  bor, 255, 0.3);(255, 255: rgbaackground
  bb {r-thumollbacrwebkit-s.toast::-.1);
}

255, 0, 255, 55rgba(2und: {
  backgrolbar-track it-scrol-webk
.toast:: 4px;
}
width:ar {
  t-scrollboast::-webki
.t*/ontent  cr longr folba scrolCustom

/* px;
}ne-offset: 2);
  outli, 0.5 255, 25555,lid rgba(2line: 2px sos {
  outon:focu
butt */sibilityfor accesus styles
}

/* Foce; 0.2s easion: all  transit 0.15);
0,(0, 0, gba25px row: 0 8px shad
  box-px);-2 translateY(orm:transfover {
  /
.toast:hr effects *ve Ho
}

/*g);
  }0de(36rm: rotate transfo  to {

  }
  eg);te(0d rotansform: {
    tra
  fromrames spin {
}

@keyfar infinite;pin 1s lineanimation: s"] {
  g-loadindiclass*="m .v-icon[*/
.toastding type n for loa animationgoadi

/* L }
}een;
 etwent: space-bustify-cont0%;
    j10 width: enter;
   ems: c-itgn   aliw;
 irection: ro flex-dd {
   st-chillex:lar .tw-f  .banne
  }

m;re  gap: 1;
  flex-startitems:
    align-lumn;tion: colex-direcex {
    f-flbanner .tw

  .ne;
  }th: noax-wid
    m
  .toast { }
 1rem;
   top: em;
      right: 1r1rem;

    left: ntainer {
  .toast-co{40px) idth: 6media (max-ws */
@stmentsive adju* Respon.1);
}

/ 255, 0a(255, 255,lid rgbom: 1px sobott  border-10px);
r(r: blufilteop- backdranner {
 yling */
.bnner StBa
/*
}
1);55, 255, 0.55, 2ba(2rg: 1px solid ;
  borderblur(10px)op-filter: kdrbacst {

.toaing */t Styl

/* Toas
}m 0.3s ease; transforn:nsitio  traast-move {
.to

;
}  opacity: 0
teX(100%);ranslasform: ttran
  ave-to {ast-le.to
}

: 0;
  opacitylateX(100%);orm: trans {
  transf-enter-fromoast

.tase-in;
}ll 0.3s eansition: a  tr-active {
oast-leave

.tse-out;
}ea all 0.3s  transition:
 ive {ctoast-enter-aions */
.tt Animatd>
/* Toas scopele
<sty>
</script
}
   }
  }s.info}`
 asseypeCl || ttype]tification.peClasses[nos} ${tyeClasse{bas  return `$
      }0'
    -bg-gray-60g: 'twloadin
  ',bg-blue-600info: 'tw-
      ge-600',oran-bg-ng: 'twni  war',
      g-red-600w-bor: 't   err   ,
  g-green-600'ccess: 'tw-b
        su {ses =eClasonst typ      cw-lg'
w-shadoite twh-text-= 'twasses eCl basonst  c{
    ion) icat(notiflasses  getBannerC
  },
    `
    .info}eClassestype] || typion.s[notificat{typeClasseasses} $rn `${baseCltu
      re00'
      }-bg-gray-5ing: 'twload
        blue-500', 'tw-bg-      info:
  -500',ge'tw-bg-oranng: rni wa   00',
    tw-bg-red-5 error: '  ',
     bg-green-500w-ess: 't      succes = {
  Class const type'
     itext-wh = 'tw-teaseClasses    const bion) {
  notificatasses(astCl
    getTo
    o
    },nf| colors.itype] |olors[return c  }

    ry'ng: 'prima      loadi
   'primary',nfo:
        irning',wa  warning: '   r',
   rror: 'erro
        e,cess': 'suc    successrs = {
     const colo
     (type) {ctionColor  getA

  info
    },olors.e] || cors[typolurn cet
      r
      }imary'g: 'prloadin
  nfo',  info: 'i
    g',arninarning: 'w    w,
    rror'or: 'e err
       ss',cess: 'succe    suc = {
    const colors
      or(type) {ColtIcon   ge
 ,
     }o
  icons.infe] ||icons[typ return
         }
  ading'-loading: 'mdi       lon',
 matio: 'mdi-infor  info
 lert',g: 'mdi-a    warnin   circle',
 -alert-error: 'mdi
      cle',check-ciri-ess: 'md        succns = {
   const ico  pe) {
 etIcon(ty
    g
     },()
loseModal.cis
      th }ck()
     n.callba    actio  lback) {
  ion.cal if (acttion)
     -action', acit('modalthis.$em
 (action) {alActionod  handleM

     },     }
 )
 er(closeBann  this.  e) {
     falsnClick !==seOlof (action.c      i      }
()
allbackn.ctio       acack) {
 lbcalion. (act
      ifction)on', anner-acti$emit('bas.     thi
 on) {rAction(actinneleBa
    hand    },
    )
      }
Idionn(notificatficatioremoveNoti this.   e) {
    als !== fOnClickosection.cl if (a}
     )
      lback(ion.calct a) {
       ckballon.ca   if (action })
   onId, actinotificatiction', { ification-as.$emit('not     thiction) {
 ionId, aotificatAction(ndle   han
     },
  dal = null
is.currentMo      th
on = falseicatiotifhis.modalN{
      tl()  closeModa
      },
 = null
   ionnerNotificat  this.ban
     {r()eBanne    clos },

   }
   1)
   e(index, ic.splnsnotificatiois.      th> -1) {
   if (index = id)
     => n.id ==ndIndex(n ications.fi= this.notif index onst) {
      c(idtionNotificaemove
    r
},ue
    n = trioificatodalNothis.m     t }

        ]imary' }r: 'prK', colo 'O'ok', label:key:         {
  tions || [ns.acios: opt  action
      ons.message,age: optiess        m
tle,s.ti: optionle   titnfo',
     'i || tions.type  type: op
     = {dalMothis.current) {
      (optionsModalow   sh },

 }

      ] || [onsptions.acti actions: o       ,
 falseable !==.closions: optlosable     c  e,
 ns.messag: optio    messagee,
    titls.optione: itl
        tinfo',ype || '.ttions opype:
        tn = {iocatnerNotifithis.ban     {
  ptions)er(oBann  show
