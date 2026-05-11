
const lbToKg = (lb) => {
  return lb * 0.4535924
}

const kgToLb = (kg) => {
  return kg / 0.4535924
}

console.log("What the fuck?")
const state = new Proxy(
  { reps: 8,
    weight: 15,
    config: {
      unit: 'lb',
      repRange: {start: 5, end: 15},
      weightRange: {start: -15, end: 20},
      idealChange: 0.05,
      increment: 1.0
    }
   },
  {
    set(target, key, value) {
      console.log(target)
      target[key] = value
      render()
      return true
    }
  }
)

const state2 = new Proxy(
  { state: {
      reps: 8,
      weight: 15,
      weightMin: 5,
      unit: 'lb',
      minRepsToConsider: 5,
      maxRepsToConsider: 16,
      weightRange: {start: -15, end: 20},
      idealChange: 0.03,
      stdIncrement: 5.0,
      micoIncrement: 1.25
    }
  },
  {
    set(target, key, value) {
      console.log(target)
      target[key] = value
      render()
      return true
    }
  }
)


const figureItOut = (state) => {
  const current = state.reps * state.weight
  const totalWithExtraRep = (state.reps + 1) * state.weight
  const totalWithExtraWeight = state.reps * (state.weight + state.stdIncrement)

  const percentChangeForExtraRep = (totalWithExtraRep-current) / current
  const percentChangeForExtraWeight = (totalWithExtraWeight-current) / current

  const lowerBound = Math.max(state.weight + state.weightRange.start, state.weightMin)
  const upperBound = state.weight + state.weightRange.end

  console.log('lowerBound', lowerBound)
  console.log('upperBound', upperBound)

  const repCounts = [...Array(state.maxRepsToConsider - state.minRepsToConsider)
      .keys().map(x => x + state.minRepsToConsider)];


  var rows = []
  for (var weight = lowerBound; weight < upperBound; weight++) {
    var row = [{type: 'weight', value: weight}]
    for (var reps = state.minRepsToConsider; reps < state.maxRepsToConsider; reps++) {
      var thisEffort = reps * weight
      var percentChange = ((thisEffort - current) / current)
      var rounded = Math.round(percentChange * 100) / 100
      row.push({
        type: 'analysis',
        thisEffort: thisEffort,
        percentChange: percentChange,
        closeToIdeal: percentChange > 0 && percentChange <= state.idealChange,
        // within 1% in either direction
        almostEquivalentToExtraRep: percentChangeForExtraRep < (percentChange * 1.01) && percentChangeForExtraRep > (percentChange * 0.99),
      })
    }
    rows.push(row);
  }
  return {
    currentEffort: current,
    percentChangeForExtraRep: percentChangeForExtraRep,
    percentChangeForExtraWeight: percentChangeForExtraWeight,
    candidates: {
      header: repCounts,
      body: rows
    }
  }
}




console.log(state)

function total(reps, weight) {
  return reps * weight
}

function renderTable(table) {
  return `
    <table>
      <thead>
        <tr>
          <th></th>
          <th colspan=${table.header.length}>Repsz</th>
        </tr>
        <tr>
          <th>Weight</th>
          ${table.header.map(rep => `
            <th>${rep}</th>
          `)}
        </tr>
      </thead>
      <tbody>
        ${table.body.map(row => `
          <tr>
            ${row.map(col => `
              ${col.type === 'weight'
              ? <td>${col.value}</td>
              : <td></td>}
            `)}
          </tr>

        `)}
      </tbody>
    </table>
  `
}





//
//function rend() {
//  <table>
//    <thead>
//      <tr>
//          <th></th>
//          <th colspan="3">Reps</th>
//      </tr>
//      <tr>
//          ${[1,2,3,4,5,6,7,8,9,10].map(repCount => `
//              <tr><td>${repCount}</td></tr>
//          `)}
//      </tr>
//    </thead>
//    <tbody>
//      ${rows.map(r => `
//      <tr>
//          <td>${r.name}</td>
//          <td>${r.score}</td>
//      </tr>
//      `).join("")}
//    </tbody>
//  </table>
//  `
//}

const render = (state) => {
  return `
    <input type="number" id="reps" value="${state.reps}">
    <input type="number" id="weight" value="${state.weight}">
    <label>
        Unit
        <select>
            <option value="lb" selected>LBs</option>
            <option value="kg">KGs</option>
        </select>
    </label>
    <button id="compute">Compute</button>
    ${renderTable(figureItOut(state).candidates)}
  `
}



//function render() {
//  figureItOut(state.reps, state.weight);
////  console.log("Hai?")
////  document.getElementById("output").textContent = total(state.reps, state.weight)
//}

//console.log(document.getElementById("reps"))
//document.getElementById("reps").addEventListener("input", e => {
//  state.reps = Number(e.target.value)
//})
//
//document.getElementById("weight").addEventListener("input", e => {
//  state.weight = Number(e.target.value)
//})
//
//console.log(state)
//
const app = document.getElementById('app');
console.log(app)
const updateDOM = (html) => {
  app.innerHTML = html
}


updateDOM(render(state2.state))

console.log(state2.state)
console.log(figureItOut(state2.state))

//renderTable(figureItOut(state2.state).candidates)