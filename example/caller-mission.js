'use strict'

const sugoCaller = require('sugo-caller')
const co = require('co')
const asleep = require('asleep')

const HUB = process.env.HUB || 'http://localhost:8080'
const ACTOR = process.env.ACTOR || 'arducopter:1'
const DRONE_TYPE = process.env.DRONE_TYPE || 'udp'
const DRONE_ADDR = process.env.DRONE_ADDR || 'localhost'

co(function * () {
  const caller = sugoCaller(HUB + '/callers')
  const actor = yield caller.connect(ACTOR)
  const arduCopter = actor.get('arduCopter')

  const takeoffAlt = 10
  const maxAlt = 50
  const moveDist = 0.001

  var gps
  arduCopter.on('gpsPosition', data => { gps = data.coordinate })

  var mission
  arduCopter.on('mode', data => {
    if (data.mode.toUpperCase() === 'GUIDED' && typeof mission === 'undefined') {
      console.log('SET MISSION')

      const goal = [gps[0] + moveDist, gps[1], 0]
      const mission = [{
        type: 'takeoff',
        altitude: takeoffAlt
      }, { // 上昇
        type: 'waypoint',
        coordinate: [0, 0, maxAlt]
      }, {
        type: 'waypoint',
        coordinate: goal
      }, {
        type: 'circle',
        coordinate: goal,
        radius: 25,
        turns: 2
      }, {
        type: 'land'
      }]
      arduCopter.saveMission(mission)
    }
  })

  arduCopter.on('missionSaved', () => {
    console.log('START MISSION')

    arduCopter.startMission(true, true)

    arduCopter.on('arming', data => {
      if (!data.arming) {
        console.log('DISCONNECT')
        caller.disconnect()
      }
    })
  })

  arduCopter.on('commandReached', data => console.log('DO COMMAND ' + data.index))

  yield arduCopter.connect(DRONE_TYPE, DRONE_ADDR)
  yield asleep(5000)
  yield arduCopter.setMode('GUIDED')
}).catch((err) => console.error(err))
