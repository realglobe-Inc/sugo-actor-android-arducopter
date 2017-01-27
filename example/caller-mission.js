'use strict'

const sugoCaller = require('sugo-caller')
const co = require('co')
const asleep = require('asleep')

const HUB = process.env.HUB || 'http://localhost:8080'
const ACTOR = process.env.ACTOR || 'arducopter:1'
const DRONE_TYPE = process.env.DRONE_TYPE || 'udp'
const DRONE_ADDR = process.env.DRONE_ADDR || 'localhost'

const takeoffAlt = 10
const alt = 30
const dist = 0.0003
const radius = 10
const turns = 2

co(function * () {
  const caller = sugoCaller(HUB + '/callers')
  const actor = yield caller.connect(ACTOR)
  const arduCopter = actor.get('ArduCopter')

  arduCopter.on('mode', data => console.log(JSON.stringify(data)))
  arduCopter.on('missionSaved', () => console.log('MISSION SAVED'))
  arduCopter.on('commandReached', data => console.log(JSON.stringify(data)))

  let coordinate
  arduCopter.on('position', data => {
    console.log(JSON.stringify(data))
    coordinate = data.coordinate
  })

  arduCopter.on('disarmed', () => co(function *() {
    console.log('DISARMED')
    yield arduCopter.disconnect()
    yield asleep(1000)
    yield caller.disconnect()
  }).catch((err) => {
    console.error(err)
    caller.disconnect()
  }))

  yield arduCopter.disableEvents(null)
  yield arduCopter.enableEvents([
    'commandReached',
    'disarmed',
    'missionSaved',
    'mode',
    'position'
  ])

  yield arduCopter.connect(DRONE_TYPE, DRONE_ADDR)
  yield asleep(3000)
  yield arduCopter.setMode('Guided')
  while (true) {
    yield asleep(1000)
    if (typeof coordinate !== 'undefined') {
      break
    }
  }

  console.log('SAVE MISSION')
  yield arduCopter.saveMission([{
    type: 'takeoff',
    altitude: takeoffAlt
  }, { // 上昇
    type: 'waypoint',
    coordinate: [0, 0, alt]
  }, {
    type: 'waypoint',
    coordinate: [coordinate[0] + dist, coordinate[1], 0]
  }, {
    type: 'circle',
    radius,
    turns
  }, {
    type: 'land'
  }])
  yield asleep(1000)
  console.log('START MISSION')
  yield arduCopter.startMission(true, true)
}).catch((err) => console.error(err))
