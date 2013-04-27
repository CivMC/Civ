from ConfigObjects import Recipe, Enchant, ItemStack, Factory
from ParseConfig import ParseConfig

import pydot
gMod=1#Global Modifier
gpMod=1#Global Production Modifier
guMod=.05#Global Upgrade Modifier
gmMod=1#Global Maintenance Modifier
def main():
    print 'Running....'
    createGraphs()
    createConfigFile()
def createConfigFile():
    config={}
    config['central_block']='WORKBENCH'
    config['save_cycle']='15'
    config['return_build_materials']='false'
    config['citadel_enabled']='false'
    config['factory_interaction_material']='STICK'
    config['destructible_factories']='false'
    config['update_cycle']='20'
    config['maintenance_cycle']='15'
    config['maintenance_period']='100'
    
    config['factories']={}
    config['recipes']={}
    specificConfigs=[]
    specificConfigs.append(createArmor())
    specificConfigs.append(createTools())
    specificConfigs.append(createMetalTech())
    specificConfigs.append(createFood())
    specificConfigs.append(createEnchanting())
    specificConfigs.append(createCauldron())
    for specificConfig in specificConfigs:
        config['factories']=dict(config['factories'].items()+specificConfig['factories'].items())
        config['recipes']=dict(config['recipes'].items()+specificConfig['recipes'].items())
    
    config['disabled_recipes']=[]
    notDisabled=['STONE']
    for recipe in config['recipes'].values():
        for output in recipe.outputs:
            if output.material not in notDisabled and output.material not in config['disabled_recipes']:
                config['disabled_recipes'].append(output.material)
    ParseConfig.saveConfig(config)
def createGraphs():
    configs={'Tools and Armor':merge(createTools(),createArmor()),'Metal Technology':createMetalTech(),'Enchanting':createEnchanting(),'Enchanting Technology':createCauldron(),'Food Technology':createFood()}
    graph=pydot.Dot("Main")
    graph.set('rankdir','LR')
    graph.add_subgraph(ParseConfig.createGraph(configs['Tools and Armor'],'cluster_Tools_And_Armor'))
    graph.add_subgraph(ParseConfig.createGraph(configs['Metal Technology'],'cluster_MetalTech'))
    graph.add_subgraph(ParseConfig.createGraph(configs['Enchanting Technology'],'cluster_Enchanting'))
    graph.add_subgraph(ParseConfig.createGraph(configs['Enchanting'],'cluster_Enchanting'))
    graph.add_subgraph(ParseConfig.createGraph(configs['Food Technology'],'cluster_food'))
    graph.write('graph.gv')
    graph.write_png('graph.png')
    for name,config in configs.items():
        graph=pydot.Dot("Main")
        graph.set('rankdir','LR')
        graph.set_label(name)
        graph.set_fontsize('24')
        graph.add_subgraph(ParseConfig.createGraph(config,'cluster_'+name))
        graph.write(name+'.gv')
        graph.write_png(name+'.png')
def merge(dict1,dict2):
    for key in dict1.keys():
        if key in dict2.keys():
            dict1[key]=dict(dict1[key].items()+dict2[key].items())
    return dict1
def createConfig():
    trees=['Food','Armory','Tools','Enchanting','Metals']
    configs={}
    configs['Food']=createFood()
    configs['Cauldron']=createCauldron()
    configs['Armory']=createArmory()
    configs['Tools']=createTools()
    configs['Enchanting']=createEnchanting()
    configs['Metals']=createMetalTech()
    recipes=[]
    factories=[]
    for config in configs.values():
        recipes=recipes+config['recipes']
        factories=factories+config['factories']
    config={'recipes':recipes,'factories':factories}
    ParseConfig.saveConfig('my_config.yml',config)
    
    graph=pydot.Graph("Main")
    graph.set('rankdir','LR')
    
    foodSubgraphs={}
    foodNames=['Food','Enchanting','Cauldron']
    for name in foodNames:
        foodSubgraphs[name]=createGraph('cluster_'+name,configs[name])
    for subgraph in foodSubgraphs.values():
        subgraph.set('fontsize','24')
    foodSubgraphs['Food'].set('label','<<B>Food Tree</B>>')
    foodSubgraphs['Enchanting'].set('label','<<B>Enchanting Tree</B>>')
    foodSubgraphs['Cauldron'].set('label','<<B>Enchanting Resource Tree</B>>')
    graphFood=pydot.Subgraph('cluster_Food')
    graphFood.set('label','<<B>Food Based Tech Trees</B>>')
    graphFood.set('fontsize','24')
    graphFood.add_subgraph(foodSubgraphs['Food'])
    graphFood.add_subgraph(foodSubgraphs['Enchanting'])
    graphFood.add_subgraph(foodSubgraphs['Cauldron'])
    graph.add_subgraph(graphFood)
    
    metalSubgraphs={}
    metalNames=['Armory','Tools','Metals']
    for name in metalNames:
        metalSubgraphs[name]=createGraph('cluster_'+name,configs[name])
    for subgraph in metalSubgraphs.values():
        subgraph.set('fontsize','24')
    metalSubgraphs['Armory'].set('label','<<B>Armor Tree</B>>')
    metalSubgraphs['Tools'].set('label','<<B>Tools and Weapons Tree</B>>')
    metalSubgraphs['Metals'].set('label','<<B>Mining Research Tree</B>>')
    graphMetal=pydot.Subgraph('cluster_Metal')
    graphMetal.set('label','<<B>Mining Based Tech Trees</B>>')
    graphMetal.set('fontsize','24')
    graphMetal.add_subgraph(metalSubgraphs['Armory'])
    graphMetal.add_subgraph(metalSubgraphs['Metals'])
    graphMetal.add_subgraph(metalSubgraphs['Tools'])
    graphMetal.set('rankdir','LR')
    graph.add_subgraph(graphMetal)
    
    filename='graph.gv'
    file=open(filename,'w')
    file.write(graph.to_string())  

def createArmor():
    factories={}
    recipes={}
    aMod=16*gMod#modifier for armor recipes
    pMod=2*gpMod#Modifier for production recipes
    uMod=1*guMod
    eMod=5#Modified for efficient upgrade
    eff=.5#Decrease in cost of efficient plant
    bulk=10#Bulk amount produced by efficient plant
    mMod=0.1*gmMod#Percent of the original cost required for maintenance
    iCosts={'Helmet':.625,'Chestplate':1,'Leggings':.875,'Boots':.5}# Modifier for different branches of the tree, based on vanilla costs
    uCosts={'Leather':1,'Iron':2,'Gold':4,'Diamond':8}#Modifier for upgrades for the different tiers of the tree
    pCosts={'Leather':1,'Iron':1,'Gold':1,'Diamond':1}#Modifier for production for the different tiers of the tree
    techs=['Leather','Iron','Gold','Diamond']
    techMat={'Leather':'Leather','Iron':'Iron Block','Gold':'Gold Block','Diamond':'Diamond Block'}
    items=['Helmet','Chestplate','Leggings','Boots']

    for i in reversed(range(len(techs))):
        tech=techs[i]
        for item in items:
            #Standard Production Recipe
            inputs0=[ItemStack(name=techMat[tech],amount=aMod*pMod*iCosts[item]*pCosts[tech])]
            outputs0=[ItemStack(name=tech+' '+item,material=tech.upper()+'_'+item.upper())]
            recipes[tech+item]=Recipe(identifier=tech+item,name='Forge '+tech+' '+item,inputs=inputs0,outputs=outputs0)
            #Efficient Production Recipe
            inputs1=[ItemStack(name=techMat[tech],amount=aMod*pMod*iCosts[item]*bulk*eff*pCosts[tech])]
            outputs1=[ItemStack(name=tech+' '+item,material=tech.upper()+'_'+item.upper(),amount=bulk)]
            maintenance=mMod*aMod*eMod*iCosts[item]*uCosts[tech]
            recipes['E'+tech+item]=Recipe(identifier='E'+tech+item,name='Forge '+tech+' '+item+'s Efficiently',inputs=inputs1,outputs=outputs1,maintenance=maintenance,useOnce=True)
            #Upgrade to Efficient Production
            inputs2=[ItemStack(name='Coal',amount=aMod*uMod*eMod*iCosts[item]*uCosts[tech])]
            outputRecipes2=[recipes['E'+tech+item]]
            recipes['UE'+tech+item]=Recipe(identifier='UE'+tech+item,name='Upgrade to Efficent '+tech+' '+item+' Production',inputs=inputs2,outputRecipes=outputRecipes2,useOnce=True)
            #Upgrade to Standard Production
            inputs3=[ItemStack(name='Coal',amount=aMod*uMod*iCosts[item]*uCosts[tech])]
            outputRecipes3=[recipes[tech+item],recipes['UE'+tech+item]]
            if tech!='Diamond':
                outputRecipes3.append(recipes['U'+techs[i+1]+item])
            recipes['U'+tech+item]=Recipe(identifier='U'+tech+item,name='Upgrade to '+tech+' '+item+' Production',inputs=inputs3,outputRecipes=outputRecipes3)
    for item in items:
        factories[item+'Smithy']=Factory(identifier=item+'Smithy',name=item+' Smithy',fuel=ItemStack(name='Charcoal'),inputs=[ItemStack(name='Stone',amount=items.index(item)+5)],maintenanceInputs=[ItemStack(name='Coal')])
        factories[item+'Smithy'].addRecipe(recipes['U'+'Leather'+item])
    return {'factories':factories,'recipes':recipes}                 
    
def createTools():
    factories={}
    recipes={}
    tools=['Sword','Axe','Pickaxe','Spade']#Damage goes 6,5,4,3
    techs=['Stone','Iron','Gold','Diamond']
    resources={'Stone':'Stone','Iron':'Iron Block','Gold':'Gold Block','Diamond':'Diamond Block'}
    
    tMod=16*gMod#modifier for tool recipes
    pMod=2*gpMod#Modifier for production recipes
    uMod=1*guMod
    eMod=5#Modified for efficient upgrade
    eff=.5#Decrease in cost of efficient plant
    bulk=10#Bulk amount produced by efficient plant
    mMod=0.3*gmMod#Percent of the original cost required for maintenance
    iCosts={'Sword':1,'Axe':.5,'Pickaxe':.75,'Spade':.25}# Modifier for different branches of the tree
    uCosts={'Stone':1,'Iron':2,'Gold':4,'Diamond':8}#Modifier for the different tiers of the tree
    pCosts={'Stone':1,'Iron':1,'Gold':1,'Diamond':1}#Modifier for production for the different tiers of the tree
    for tech in reversed(techs):
        for tool in tools:
            #Efficient Production Recipe
            inputs=[ItemStack(name=resources[tech],amount=tMod*pMod*iCosts[tool]*bulk*eff*pCosts[tech])]
            outputs=[ItemStack(material=tech.upper()+'_'+tool.upper(),name=tech+' '+tool,amount=bulk,durability=-218*(tech=='Gold'))]
            maintenance=tMod*mMod*eMod*iCosts[tool]*uCosts[tech]
            recipes['E'+tech+tool]=Recipe('E'+tech+tool,name='Forge '+tech+' '+tool+'s Efficiently',inputs=inputs,outputs=outputs,maintenance=maintenance)
            #Upgrade to an Efficient Factory Recipe
            inputs=[ItemStack(name='Coal',amount=tMod*eMod*iCosts[tool]*uCosts[tech])]
            outputRecipes=[recipes['E'+tech+tool]]
            recipes['UE'+tech+tool]=Recipe('UE'+tech+tool,name='Upgrade to Efficient '+tech+' '+tool+' production',inputs=inputs,outputRecipes=outputRecipes,useOnce=True)
            #Standard Production Recoipe
            inputs=[ItemStack(name=resources[tech],amount=tMod*uMod*pMod*iCosts[tool]*pCosts[tech])]
            outputs=[ItemStack(material=tech.upper()+'_'+tool.upper(),name=tech+' '+tool,durability=-218*(tech=='Gold'))]
            recipes[tech+tool]=Recipe(tech+tool,name='Forge '+tech+' '+tool,inputs=inputs,outputs=outputs)
            #Upgrade to production factory
            inputs=[ItemStack(name='Coal',amount=tMod*iCosts[tool]*uCosts[tech])]
            outputRecipes=[recipes[tech+tool],recipes['UE'+tech+tool]]
            if tech!='Diamond':
                outputRecipes+=[recipes['U'+techs[techs.index(tech)+1]+tool]]
            recipes['U'+tech+tool]=Recipe('U'+tech+tool,name='Upgrade to '+tech+' '+tool+' Production',inputs=inputs,outputRecipes=outputRecipes,useOnce=True)
    for tool in tools:
        factories[tool+"Smithy"]=Factory(identifier=tool+"Smithy",name=tool+' Smithy',fuel=ItemStack(name='Charcoal'),inputs=[ItemStack(name='Stone',amount=tools.index(tool)+1)],maintenanceInputs=[ItemStack(name='Coal')])
        factories[tool+"Smithy"].addRecipe(recipes['U'+'Stone'+tool])
    return {'factories':factories,'recipes':recipes}
  
def createMetalTech():
    factories={}
    recipes={}
    techLevels=['Stone','Iron','Gold','Diamond','Emerald']
    techInputs={'Stone':('Cobblestone','COBBLESTONE'),'Iron':('Iron Ore','IRON_ORE'),'Gold':('Gold Ore','GOLD_ORE'),'Diamond':('Diamond','DIAMOND'),'Emerald':('Emerald','EMERALD')}
    techOutputs={'Stone':'Stone','Iron':'Iron Block','Gold':'Gold Block','Diamond':'Diamond Block','Emerald':'Emerald Block'}
    
        
    eMod=5#Modified for efficient upgrade
    meMod=1*gMod#modifier for MetalTech recipes
    pMod=1*gpMod#Modifier for producing items
    uMod=1*guMod#Modifier for upgrades
    eff=.8#Decrease in cost of efficient plant
    bulk=64#Bulk amount produced by efficient plant
    mMod=0.1*gmMod#Percent of the original cost required for maintenance
    iMod={'Stone':1,'Iron':1,'Gold':1,'Diamond':1,'Emerald':1}
    oMod={'Stone':1,'Iron':1,'Gold':1,'Diamond':1,'Emerald':1}
    uCost={'Stone':40,'Iron':40,'Gold':40,'Diamond':40,'Emerald':40}
    
    for i in reversed(range(len(techLevels))):
        tech=techLevels[i]
        #Efficient Recipe
        inputs=[ItemStack(name=techInputs[tech][0],material=techInputs[tech][1],amount=meMod*pMod*iMod[tech]*bulk*eff)]
        outputs=[ItemStack(name=techOutputs[tech],amount=meMod*oMod[tech]*bulk)]
        maintenance=gMod*mMod*uCost[tech]*eMod if tech=='Diamond' or tech=='Emerald' else None
        recipes['E'+tech]=Recipe(identifier='E'+tech,name='Process '+techInputs[tech][1]+' Efficiently',inputs=inputs,outputs=outputs,maintenance=maintenance)
        #Standard Recipe
        inputs=[ItemStack(name=techInputs[tech][0],material=techInputs[tech][1],amount=meMod*pMod*iMod[tech])]
        outputs=[ItemStack(name=techOutputs[tech],amount=meMod*oMod[tech])]
        recipes[tech]=Recipe(identifier=tech,name='Process '+techInputs[tech][1],inputs=inputs,outputs=outputs)
        #Upgrade to efficient Recipe
        inputs=[ItemStack(name=techOutputs[tech],amount=meMod*uMod*uCost[tech]*eMod)]
        outputRecipes=[recipes['E'+tech]]
        recipes['UE'+tech]=Recipe(identifier='UE'+tech,name='Upgrade to Efficient '+tech+' Processing',inputs=inputs,outputRecipes=outputRecipes,useOnce=True)
        #Upgrade to standard recipe
        inputs=[ItemStack(name=techInputs[tech][0],material=techInputs[tech][1],amount=meMod*uMod*uCost[tech])]
        outputRecipes=[recipes[tech],recipes['UE'+tech]]
        if i!=len(techLevels)-1:
            outputRecipes.append(recipes['U'+techLevels[i+1]])
        recipes['U'+tech]=Recipe(identifier='U'+tech,name='Upgrade to '+tech+' Processing',inputs=inputs,outputRecipes=outputRecipes,useOnce=True)
    factories['Smelter']=Factory(identifier='Smelter',name='Smelter',inputs=[ItemStack(name='Cobblestone',material='COBBLESTONE',amount=64)],maintenanceInputs=[ItemStack(name='Diamond Block',material='DIAMOND_BLOCK')])
    factories['Smelter'].addRecipe(recipes['UStone'])
    
    return {'factories':factories,'recipes':recipes}  
               
def createFood():

    fMod=1
    pMod=1*gpMod*fMod
    uMod=20*guMod*fMod
    euMod=5*uMod
    eff=.7
    bulk=64
    eInput=pMod*bulk*eff
    factories={}
    recipes={}
    inputs={}
    outputs={}
    
    maintenance=[64,64,64]*gmMod
    
    
    inputs['Golden_Apple']=[ItemStack(material='APPLE',amount=pMod),ItemStack(material='GOLD_BLOCK',amount=3*pMod)]
    outputs['Golden_Apple']=[ItemStack(material='GOLDEN_APPLE')]
    recipes['Golden_Apple']=Recipe(identifier='Golden_Apple',name='Golden Apple',inputs=inputs['Golden_Apple'],outputs=outputs['Golden_Apple'])
    inputs['E_Golden_Apple']=[item.modifyAmount(eInput) for item in inputs['Golden_Apple']]
    outputs['E_Golden_Apple']=[item.modifyAmount(bulk) for item in outputs['Golden_Apple']]
    recipes['E_Golden_Apple']=Recipe(identifier='E_Golden_Apple',name='Efficient Golden Apple',inputs=inputs['E_Golden_Apple'],outputs=outputs['E_Golden_Apple'],maintenance=maintenance[2])
    inputs['Golden_Carrot']=[ItemStack(name='Carrot',material='CARROT_ITEM',amount=pMod),ItemStack(material='GOLD_BLOCK',amount=3*pMod)]
    outputs['Golden_Carrot']=[ItemStack(material='GOLDEN_CARROT')]
    recipes['Golden_Carrot']=Recipe(identifier='Golden_Carrot',name='Golden Carrot',inputs=inputs['Golden_Carrot'],outputs=outputs['Golden_Carrot'])
    inputs['E_Golden_Carrot']=[item.modifyAmount(eInput) for item in inputs['Golden_Carrot']]
    outputs['E_Golden_Carrot']=[item.modifyAmount(bulk) for item in outputs['Golden_Carrot']]
    recipes['E_Golden_Carrot']=Recipe(identifier='E_Golden_Carrot',name='Efficient Golden Carrot',inputs=inputs['E_Golden_Carrot'],outputs=outputs['E_Golden_Carrot'],maintenance=maintenance[2])
    inputs['Beef']=[ItemStack(material='RAW_BEEF',amount=pMod)]
    outputs['Beef']=[ItemStack(material='COOKED_BEEF')]
    recipes['Beef']=Recipe(identifier='Beef',name='Beef',inputs=inputs['Beef'],outputs=outputs['Beef'])
    inputs['E_Beef']=[item.modifyAmount(eInput) for item in inputs['Beef']]
    outputs['E_Beef']=[item.modifyAmount(bulk) for item in outputs['Beef']]
    recipes['E_Beef']=Recipe(identifier='E_Beef',name='Efficient Beef',inputs=inputs['E_Beef'],outputs=outputs['E_Beef'],maintenance=maintenance[2])
    inputs['Cake']=[ItemStack(material='WHEAT',amount=pMod),ItemStack(material='SUGAR',amount=pMod),ItemStack(material='EGGS',amount=pMod)]
    outputs['Cake']=[ItemStack(material='CAKE')]
    recipes['Cake']=Recipe(identifier='Cake',name='Cake',inputs=inputs['Cake'],outputs=outputs['Cake'])
    inputs['E_Cake']=[item.modifyAmount(eInput) for item in inputs['Cake']]
    outputs['E_Cake']=[item.modifyAmount(bulk) for item in outputs['Cake']]
    recipes['E_Cake']=Recipe(identifier='E_Cake',name='Efficient Cake',inputs=inputs['E_Cake'],outputs=outputs['E_Cake'],maintenance=maintenance[1])
    inputs['Pumpkin_Pie']=[ItemStack(material='PUMPKIN',amount=pMod),ItemStack(material='SUGAR',amount=pMod),ItemStack(material='EGGS',amount=pMod)]
    outputs['Pumpkin_Pie']=[ItemStack(material='PUMPKIN_PIE')]
    recipes['Pumpkin_Pie']=Recipe(identifier='Pumpkin_Pie',name='Pumpkin Pie',inputs=inputs['Pumpkin_Pie'],outputs=outputs['Pumpkin_Pie'])
    inputs['E_Pumpkin_Pie']=[item.modifyAmount(eInput) for item in inputs['Pumpkin_Pie']]
    outputs['E_Pumpkin_Pie']=[item.modifyAmount(bulk) for item in outputs['Pumpkin_Pie']]
    recipes['E_Pumpkin_Pie']=Recipe(identifier='E_Pumpkin_Pie',name='Efficient Pumpkin Pit',inputs=inputs['E_Pumpkin_Pie'],outputs=outputs['E_Pumpkin_Pie'],maintenance=maintenance[1])
    inputs['Pork']=[ItemStack(material='PORK',amount=pMod)]
    outputs['Pork']=[ItemStack(material='GRILLED_PORK')]
    recipes['Pork']=Recipe(identifier='Pork',name='Pork',inputs=inputs['Pork'],outputs=outputs['Pork'])
    inputs['E_Pork']=[item.modifyAmount(eInput) for item in inputs['Pork']]
    outputs['E_Pork']=[item.modifyAmount(bulk) for item in outputs['Pork']]
    recipes['E_Pork']=Recipe(identifier='E_Pork',name='Efficient Pork',inputs=inputs['E_Pork'],outputs=outputs['E_Pork'],maintenance=maintenance[1])
    inputs['Chicken']=[ItemStack(material='RAW_CHICKEN',amount=pMod)]
    outputs['Chicken']=[ItemStack(material='COOKED_CHICKEN')]
    recipes['Chicken']=Recipe(identifier='Chicken',name='Chicken',inputs=inputs['Chicken'],outputs=outputs['Chicken'])
    inputs['E_Chicken']=[item.modifyAmount(eInput) for item in inputs['Chicken']]
    outputs['E_Chicken']=[item.modifyAmount(bulk) for item in outputs['Chicken']]
    recipes['E_Chicken']=Recipe(identifier='E_Chicken',name='Efficient Chicken',inputs=inputs['E_Chicken'],outputs=outputs['E_Chicken'],maintenance=maintenance[1])
    inputs['Baked_Potato']=[ItemStack(name='Potato',material='POTATO_ITEM',amount=pMod)]
    outputs['Baked_Potato']=[ItemStack(material='BAKED_POTATO')]
    recipes['Baked_Potato']=Recipe(identifier='Baked_Potato',name='Baked Potato',inputs=inputs['Baked_Potato'],outputs=outputs['Baked_Potato'])
    inputs['E_Baked_Potato']=[item.modifyAmount(eInput) for item in inputs['Baked_Potato']]
    outputs['E_Baked_Potato']=[item.modifyAmount(bulk) for item in outputs['Baked_Potato']]
    recipes['E_Baked_Potato']=Recipe(identifier='E_Baked_Potato',name='Efficient Baked Potato',inputs=inputs['E_Baked_Potato'],outputs=outputs['E_Baked_Potato'],maintenance=maintenance[0])
    inputs['Bread']=[ItemStack(material='WHEAT',amount=pMod),ItemStack(material='SUGAR',amount=pMod)]
    outputs['Bread']=[ItemStack(material='BREAD')]
    recipes['Bread']=Recipe(identifier='Bread',name='Bread',inputs=inputs['Bread'],outputs=outputs['Bread'])
    inputs['E_Bread']=[item.modifyAmount(eInput) for item in inputs['Bread']]
    outputs['E_Bread']=[item.modifyAmount(bulk) for item in outputs['Bread']]
    recipes['E_Bread']=Recipe(identifier='E_Bread',name='Efficient Bread',inputs=inputs['E_Bread'],outputs=outputs['E_Bread'],maintenance=maintenance[0])
    inputs['Cookie']=[ItemStack(name='Cocoa',amount=pMod),ItemStack(name='Wheat',material='WHEAT',amount=pMod)]
    outputs['Cookie']=[ItemStack(name='Cookie!',material='COOKIE',amount=3*fMod)]
    recipes['Cookie']=Recipe(identifier='Cookie',name='Cookies!',inputs=inputs['Cookie'],outputs=outputs['Cookie'])
    inputs['E_Cookie']=[item.modifyAmount(eInput) for item in inputs['Cookie']]
    outputs['E_Cookie']=[item.modifyAmount(bulk) for item in outputs['Cookie']]
    recipes['E_Cookie']=Recipe(identifier='E_Cookie',name='Efficient Cookies!',inputs=inputs['E_Cookie'],outputs=outputs['E_Cookie'])
    inputs['Mushroom_Soup']=[ItemStack(name='Brown Mushroom',material='BROWN_MUSHROOM',amount=pMod),ItemStack(name='Red Mushroom',material='RED_MUSHROOM',amount=pMod)]
    outputs['Mushroom_Soup']=[ItemStack(material='MUSHROOM_SOUP',amount=3*fMod)]
    recipes['Mushroom_Soup']=Recipe(identifier='Mushroom_Soup',name='Mushroom Soup',inputs=inputs['Mushroom_Soup'],outputs=outputs['Mushroom_Soup'])
    inputs['E_Mushroom_Soup']=[item.modifyAmount(eInput) for item in inputs['Mushroom_Soup']]
    outputs['E_Mushroom_Soup']=[item.modifyAmount(bulk) for item in outputs['Mushroom_Soup']]
    recipes['E_Mushroom_Soup']=Recipe(identifier='E_Mushroom_Soup',name='Efficient Mushroom Soup',inputs=inputs['E_Mushroom_Soup'],outputs=outputs['E_Mushroom_Soup'],maintenance=maintenance[0])
    maintenance=64
    inputs['U_E_Beef']=[ItemStack(material='ENDER_PEARL',amount=uMod),ItemStack(material='GHAST_TEAR',amount=uMod)]
    recipes['U_E_Beef']=Recipe(identifier='U_E_Beef',name='Upgrade to Efficient Beef',inputs=inputs['U_E_Beef'],outputRecipes=[recipes['E_Beef']],useOnce=True)
    inputs['U_E_Golden_Carrot']=[ItemStack(material='ENDER_PEARL',amount=uMod),ItemStack(material='GHAST_TEAR',amount=uMod)]
    recipes['U_E_Golden_Carrot']=Recipe(identifier='U_E_Golden_Carrot',name='Upgrade to Efficient Golden Carrots',inputs=inputs['U_E_Golden_Carrot'],outputRecipes=[recipes['E_Golden_Carrot']],useOnce=True)
    inputs['U_Golden_Carrot_and_Beef']=[ItemStack(material='ENDER_PEARL',amount=uMod),ItemStack(material='GHAST_TEAR',amount=uMod)]
    recipes['U_Golden_Carrot_and_Beef']=Recipe(identifier='U_Golden_Carrot_and_Beef',name='Upgrade to Golden Carrots',inputs=inputs['U_Golden_Carrot_and_Beef'],outputRecipes=[recipes['Golden_Carrot'],recipes['U_E_Golden_Carrot'],recipes['Beef'],recipes['U_E_Beef']],useOnce=True)
    inputs['U_E_Golden_Apple']=[ItemStack(material='ENDER_PEARL',amount=uMod),ItemStack(material='GHAST_TEAR',amount=uMod)]
    recipes['U_E_Golden_Apple']=Recipe(identifier='U_E_Golden_Apple',name='Upgrade to Efficient Golden Apples',inputs=inputs['U_E_Golden_Apple'],outputRecipes=[recipes['E_Golden_Apple']],useOnce=True)
    inputs['U_Golden_Apple']=[ItemStack(material='ENDER_PEARL',amount=uMod),ItemStack(material='GHAST_TEAR',amount=uMod)]
    recipes['U_Golden_Apple']=Recipe(identifier='U_Golden_Apple',name='Upgrade to Golden Apples',inputs=inputs['U_Golden_Apple'],outputRecipes=[recipes['Golden_Apple'],recipes['U_E_Golden_Apple']],useOnce=True)
    inputs['U_E_Cake']=[ItemStack(material='SPIDER_EYE',amount=uMod),ItemStack(name='Gunpowder',material='SULPHUR',amount=uMod)]
    recipes['U_E_Cake']=Recipe(identifier='U_E_Cake',name='Upgrade to Efficient Cake',inputs=inputs['U_E_Cake'],outputRecipes=[recipes['E_Cake']],useOnce=True)
    inputs['U_E_Chicken']=[ItemStack(material='SPIDER_EYE',amount=uMod),ItemStack(name='Gunpowder',material='SULPHUR',amount=uMod)]
    recipes['U_E_Chicken']=Recipe(identifier='U_E_Chicken',name='Upgrade to Efficient Chicken',inputs=inputs['U_E_Chicken'],outputRecipes=[recipes['E_Chicken']],useOnce=True)
    inputs['U_Cake_and_Chicken']=[ItemStack(material='SPIDER_EYE',amount=uMod),ItemStack(name='Gunpowder',material='SULPHUR',amount=uMod)]
    recipes['U_Cake_and_Chicken']=Recipe(identifier='U_Cake_and_Chicken',name='Upgrade to Cake and Chicken',inputs=inputs['U_Cake_and_Chicken'],outputRecipes=[recipes['Cake'],recipes['U_E_Cake'],recipes['Chicken'],recipes['U_E_Chicken'],recipes['U_Golden_Apple']],useOnce=True)
    inputs['U_E_Pumpkin_Pie']=[ItemStack(material='SPIDER_EYE',amount=uMod),ItemStack(name='Gunpowder',material='SULPHUR',amount=uMod)]
    recipes['U_E_Pumpkin_Pie']=Recipe(identifier='U_E_Pumpkin_Pie',name='Upgrade to Efficient Pumpkin Pie',inputs=inputs['U_E_Pumpkin_Pie'],outputRecipes=[recipes['E_Pumpkin_Pie']],useOnce=True)
    inputs['U_E_Pork']=[ItemStack(material='SPIDER_EYE',amount=uMod),ItemStack(name='Gunpowder',material='SULPHUR',amount=uMod)]
    recipes['U_E_Pork']=Recipe(identifier='U_E_Pork',name='Upgrade to Efficient Pork',inputs=inputs['U_E_Pork'],outputRecipes=[recipes['E_Pork']],useOnce=True)
    inputs['U_Pumpkin_Pie_and_Pork']=[ItemStack(material='SPIDER_EYE',amount=uMod),ItemStack(name='Gunpowder',material='SULPHUR',amount=uMod)]
    recipes['U_Pumpkin_Pie_and_Pork']=Recipe(identifier='U_Pumpkin_Pie_and_Pork',name='Upgrade to Pumpkin Pie',inputs=inputs['U_Pumpkin_Pie_and_Pork'],outputRecipes=[recipes['Pumpkin_Pie'],recipes['U_E_Pumpkin_Pie'],recipes['Pork'],recipes['U_E_Pork'],recipes['U_Golden_Carrot_and_Beef']],useOnce=True)
    inputs['U_E_Bread']=[ItemStack(material='BONE',amount=uMod),ItemStack(material='ROTTEN_FLESH',amount=uMod)]
    recipes['U_E_Bread']=Recipe(identifier='U_E_Bread',name='Upgrade to Efficient Bread',inputs=inputs['U_E_Bread'],outputRecipes=[recipes['E_Bread']],useOnce=True)
    inputs['U_E_Baked_Potato']=[ItemStack(material='BONE',amount=uMod),ItemStack(material='ROTTEN_FLESH',amount=uMod)]
    recipes['U_E_Baked_Potato']=Recipe(identifier='U_E_Baked_Potato',name='Upgrade to Efficient Baked Potato',inputs=inputs['U_E_Baked_Potato'],outputRecipes=[recipes['E_Baked_Potato']],useOnce=True)
    inputs['U_E_Cookie']=[ItemStack(material='BONE',amount=uMod),ItemStack(material='ROTTEN_FLESH',amount=uMod)]
    recipes['U_E_Cookie']=Recipe(identifier='U_E_Cookie',name='Upgrade to Efficient Cookie',inputs=inputs['U_E_Cookie'],outputRecipes=[recipes['E_Cookie']],useOnce=True)
    inputs['U_E_Mushroom_Soup']=[ItemStack(material='BONE',amount=uMod),ItemStack(material='ROTTEN_FLESH',amount=uMod)]
    recipes['U_E_Mushroom_Soup']=Recipe(identifier='U_E_Mushroom_Soup',name='Upgrade to Efficient Mushroom Soup',inputs=inputs['U_E_Mushroom_Soup'],outputRecipes=[recipes['E_Mushroom_Soup']],useOnce=True)
        
    factories['Quaint_Bakery']=Factory(identifier='Quant_Bakery',name='Quaint Bakery',inputs=[ItemStack(name='Stick',material='STICK',amount=5)],maintenanceInputs=[ItemStack(material='CHARCOAL')])
    for recipe in [recipes['Bread'],recipes['U_E_Bread'],recipes['Cookie'],recipes['U_E_Cookie'],recipes['U_Cake_and_Chicken']]:
        factories['Quaint_Bakery'].addRecipe(recipe)
    factories['Quirky_Bakery']=Factory(identifier='Quirky_Bakery',name='Quirky Bakery',inputs=[ItemStack(name='Stick',material='STICK',amount=10)],maintenanceInputs=[ItemStack(material='CHARCOAL')])
    for recipe in [recipes['Baked_Potato'],recipes['U_E_Baked_Potato'],recipes['Mushroom_Soup'],recipes['U_E_Mushroom_Soup'],recipes['U_Pumpkin_Pie_and_Pork']]:
        factories['Quirky_Bakery'].addRecipe(recipe)
    
    
    return {'factories':factories,'recipes':recipes}
    
def createEnchanting():
    factories={}
    recipes={}
    techs=['Clear Potion','Diffuse Potion','Artless Potion','Thin Potion','Bungling Potion']
    uMod=20*guMod
    pMod=1*gpMod
    mMod=1*gmMod
    
    #Structure:Dict{branch:Dict{enchantment:Dict{level:[teir0Prob,tier1Prob,...}}
    tiers=5
    enchantments={}
    items={}
    factoryCosts={'Industrial':16,'Melee':32,'Defensive':48,'Bows':64}
    
    #enchantments['Industrial']={'DURABILITY','DIG_SPEED','SILK_TOUCH','LOOT_BONUS_BLOCKS'}
    enchantments['Industrial']={}
    items['Industrial']=['STONE_PICKAXE','IRON_PICKAXE','GOLD_PICKAXE','DIAMOND_PICKAXE','STONE_AXE','IRON_AXE','GOLD_AXE','DIAMOND_AXE','STONE_SPADE','IRON_SPADE','GOLD_PICKAXE','DIAMOND_PICKAXE']
    enchantments['Industrial']['Durability']={1:[.5,.6,.7,.8,.9],2:[0,.2,.4,.6,.8],3:[0,0,0,.1,.3]}
    enchantments['Industrial']['Silk Touch']={1:[.5,.4,.4,0,0]}
    enchantments['Industrial']['Efficiency']={1:[.5,.5,.7,.8,.9],2:[.2,.3,.4,.5,.6,.7],3:[0,.1,.2,.3,.4,.5],4:[0,0,.1,.2,.3],5:[0,0,0,0,.1]}
    enchantments['Industrial']['Fortune']={1:[0,.3,.5,.5,.5],2:[0,0,.3,.5,.5],3:[0,0,0,0,.1]}
        
    #enchantments['Melee']={'DAMAGE_ALL','DAMAGE_ANTHROPODS','KNOCKBACK','FIRE_ASPECT','LOOT_BONUS_MOBS'}
    enchantments['Melee']={}
    items['Melee']=['STONE_AXE','IRON_AXE','GOLD_AXE','DIAMOND_AXE','STONE_SWORD','IRON_SWORD','GOLD_SWORD','DIAMOND_SWORD']
    enchantments['Melee']['Sharpness']={1:[.7,.5,.3,0,0],2:[.2,.4,.5,.3,.1],3:[0,0,.3,.5,.2],4:[0,0,0,.3,.3],5:[0,0,0,0,.1]}
    enchantments['Melee']['Bane of the Anthropods']={1:[.1,.1,.1,.1,.1]}
    enchantments['Melee']['Knockback']={1:[.5,.5,.5,.3,.2],2:[0,0,.4,.5,.2]}
    enchantments['Melee']['Fire Aspect']={1:[.2,.3,.4,.4,.3],2:[0,0,0,.1,.2]}
    enchantments['Melee']['Looting']={1:[.8,.8,.5,0,0],2:[.2,.5,.7,.4,.2],3:[0,0,0,.3,.4]}
    
    #enchantments['Bows']={'ARROW_DAMAGE','ARROW_KNOCKBACK','ARROW_FIRE','ARROW_INFINITE'}
    enchantments['Bows']={}
    items['Bows']=['BOW']
    enchantments['Bows']['Power']={1:[.3,.4,.5,.4,.3],2:[.4,.5,.5,.3,.2],3:[0,.2,.3,.4,.2],4:[0,0,0,.2,.2],5:[0,0,0,0,.1]}
    enchantments['Bows']['Knockback']={1:[.1,.2,.3,.4,.4],2:[0,0,0,.1,.2]}
    enchantments['Bows']['Flame']={1:[0,0,0,.1,.2]}
    enchantments['Bows']['Infinite']={1:[.9,.7,.5,.3,.2]}
    
    #enchantments['Defensive']={'PROTECTION_FIRE','OXYGEN','PROTECTION_ENVIRONMENTAL','PROTECTION_PROJECTILE','PROTECTION_FALL'}
    enchantments['Defensive']={}
    items['Defensive']=[]
    items['Defensive']=['LEATHER_HELMET','IRON_HELMET','GOLD_HELMET','DIAMOND_HELMET']
    items['Defensive']=items['Defensive']+['LEATHER_CHESTPLATE','IRON_CHESTPLATE','GOLD_CHESTPLATE','DIAMOND_CHESTPLATE']
    items['Defensive']=items['Defensive']+['LEATHER_LEGGINGS','IRON_LEGGINGS','GOLD_LEGGINGS','DIAMOND_LEGGINGS']
    items['Defensive']=items['Defensive']+['LEATHER_BOOTS','IRON_BOOTS','GOLD_BOOTS','DIAMOND_BOOTS']
    enchantments['Defensive']['Fire Protection']={1:[.7,.5,.3,0,0],2:[.2,.4,.5,.3,.1],3:[0,0,0,.3,.3],4:[0,0,0,0,.1]}
    enchantments['Defensive']['Protection']={1:[.7,.5,.3,0,0],2:[.2,.4,.5,.3,.1],3:[0,0,0,.3,.3],4:[0,0,0,0,.1]}
    enchantments['Defensive']['Projectile Protection']={1:[.7,.5,.3,0,0],2:[.2,.4,.5,.3,.1],3:[0,0,0,.3,.3],4:[0,0,0,0,.1]}
    enchantments['Defensive']['Feather Falling']={1:[.7,.5,.3,0,0],2:[.2,.4,.5,.3,.1],3:[0,0,0,.3,.3],4:[0,0,0,0,.1]}
    enchantments['Defensive']['Respiration']={1:[.7,.5,.3,0,0],2:[.2,.4,.5,.3,.1],3:[0,0,0,.3,.3]}
    
    for branch in enchantments.keys():#Go through different classes of enchanments
        for tier in reversed(range(tiers)):#Goes thougheach tier of more advanced enchantments
            inputs=[ItemStack(techs[tier],amount=pMod)]
            upgrades=[]
            for item in items[branch]:
                upgrades.append(ItemStack(material=item))
            maintenance=mMod*uMod if tier==(tiers-1) else None
            recipes[branch+str(tier)]=Recipe(identifier=branch+str(tier),name='Level '+str(tier+1)+' '+branch+' Enchantments',inputs=inputs,upgrades=upgrades,maintenance=maintenance)
            for enchantment in enchantments[branch].keys():#Goes through different enchantments that can be applied to this class
                for level in enchantments[branch][enchantment].keys():#Goes through the different levels that can be applied to that enchant
                    recipes[branch+str(tier)].addEnchant(Enchant(name=enchantment,level=level,probability=enchantments[branch][enchantment][level][tier]))
            inputs=[ItemStack(name=techs[tier],amount=gMod*uMod)]
            outputRecipes=[recipes[branch+str(tier)]]
            if tier!=tiers-1:outputRecipes.append(recipes['U'+branch+str(tier+1)])
            recipes['U'+branch+str(tier)]=Recipe(identifier='U'+branch+str(tier),name='Upgrade to Level '+str(tier+1)+' '+branch+' Enchantments',inputs=inputs,outputRecipes=outputRecipes,useOnce=True)
    for branch in enchantments.keys():    
        inputs=[ItemStack(name='Stick',material='STICK',amount=factoryCosts[branch])]
        factories[branch+'EnchantingTable']=Factory(identifier=branch+'EnchantingTable',name=branch+' Enchanting Table',fuel=ItemStack(name='Stick',material='STICK'),inputs=inputs,maintenanceInputs=[ItemStack(name='Clear Potion')])
        factories[branch+'EnchantingTable'].addRecipe(recipes['U'+branch+'0'])
    
    
    return {'factories':factories,'recipes':recipes}
 
def createCauldron():
    factories={}
    recipes={}
    inputs={}
    outputs={}
    outputRecipes={}
    cMod=1*gMod
    pMod=1*gpMod
    uMod=10*guMod
    euMod=5
    eff=.7
    bulk=10
    mMod=64*gmMod
    maintenance=[mMod* num for num in [1,2,3,4,5]]
    techs=['Clear Potion','Diffuse Potion','Artless Potion','Thin Potion','Bungling Potion']
    tMod={'Clear Potion':1*pMod,'Diffuse Potion':1*pMod,'Artless Potion':1*pMod,'Thin Potion':1*pMod,'Bungling Potion':1*pMod}
    
    defined_inputs={}
    for tech in techs:
        defined_inputs[tech]=[None,None]
    defined_inputs[techs[0]][0]=[ItemStack(name='Wheat',material='WHEAT',amount=cMod*tMod[techs[0]]*32),ItemStack(name='Cocoa',amount=cMod*tMod[techs[0]]*32)]
    defined_inputs[techs[0]][1]=[ItemStack(name='Potato',material='POTATO_ITEM',amount=cMod*tMod[techs[0]]*32),ItemStack(name='Brown Mushroom',material='BROWN_MUSHROOM',amount=cMod*tMod[techs[0]]*8)]
    defined_inputs[techs[1]][0]=[ItemStack(name='Bread',material='BREAD',amount=cMod*tMod[techs[1]]*32),ItemStack(name='Cookies!',material='COOKIE',amount=cMod*tMod[techs[1]]*32),ItemStack(name='Potato',material='POTATO',amount=cMod*tMod[techs[1]]*8)]
    defined_inputs[techs[1]][1]=[ItemStack(name='Baked Potato',material='BAKED_POTATO',amount=cMod*tMod[techs[1]]*32),ItemStack(name='Mushroom Soup',material='MUSHROOM_SOUP',amount=cMod*tMod[techs[1]]*8),ItemStack(name='Wheat',material='WHEAT',amount=cMod*tMod[techs[1]]*8)]
    defined_inputs[techs[2]][0]=[ItemStack(name='Cake',material='CAKE',amount=cMod*tMod[techs[2]]*32),ItemStack(name='Chicken Wings',material='CHICKEN',amount=cMod*tMod[techs[2]]*16),ItemStack(name='Baked Potato',material='BAKED_POTATO',amount=cMod*tMod[techs[2]]*64)]
    defined_inputs[techs[2]][1]=[ItemStack('PUMPKIN_PIE',amount=cMod*tMod[techs[2]]*32),ItemStack('PORK',amount=cMod*tMod[techs[2]]*16),ItemStack('BREAD',amount=cMod*tMod[techs[2]]*64)]
    defined_inputs[techs[3]][0]=[ItemStack('CACTUS',amount=cMod*tMod[techs[3]]*32),ItemStack('NETHERWART',amount=cMod*tMod[techs[3]]*32),ItemStack('CAKE',amount=cMod*tMod[techs[3]]*32)]
    defined_inputs[techs[3]][1]=[ItemStack('CACTUS',amount=cMod*tMod[techs[3]]*32),ItemStack('NETHERWART',amount=cMod*tMod[techs[3]]*32),ItemStack('PUMPKIN_PIE',amount=cMod*tMod[techs[3]]*32)]
    defined_inputs[techs[4]][0]=[ItemStack('NETHERWART',amount=cMod*tMod[techs[4]]*32),ItemStack('GOLDEN_APPLE',amount=cMod*tMod[techs[4]]*32),ItemStack('STEAK',amount=cMod*tMod[techs[4]]*16)]
    defined_inputs[techs[4]][1]=[ItemStack('NETHERWART',amount=cMod*tMod[techs[4]]*32),ItemStack('GOLDEN_CARROT',amount=cMod*tMod[techs[4]]*32),ItemStack('CAKE',amount=cMod*tMod[techs[4]]*64)]
    for i in reversed(range(len(techs))):
        tech=techs[i]
        for j in [0,1]:
            identifier={}
            identifier['Potion']=tech.replace(" ", "")+str(j)
            identifier['E_Potion']='E_'+tech.replace(" ", "")+str(j)
            identifier['U_Potion']='U_'+tech.replace(" ", "")+str(j)
            identifier['U_E_Potion']='U_E_'+tech.replace(" ", "")+str(j)
            #Calculate Inputs/outputs
            #Potion
            inputs[identifier['Potion']]=defined_inputs[tech][j]
            outputs[identifier['Potion']]=[ItemStack(name=tech)]
            #E Potion
            inputs[identifier['E_Potion']]=[input.modifyAmount(bulk*eff) for input in inputs[identifier['Potion']]]
            outputs[identifier['E_Potion']]=[output.modifyAmount(bulk) for output in outputs[identifier['Potion']]]
            #U Potion
            inputs[identifier['U_Potion']]=[ItemStack(name=tech,amount=cMod*uMod)]
            #U E Potion
            inputs[identifier['U_E_Potion']]=[input.modifyAmount(euMod) for input in inputs[identifier['U_Potion']]]
            #Potion Recipe
            recipes[identifier['Potion']]=Recipe(identifier=identifier['Potion'],name='Brew '+tech,inputs=inputs[identifier['Potion']],outputs=outputs[identifier['Potion']])
            #Efficient Potion Recipe
            recipes[identifier['E_Potion']]=Recipe(identifier=identifier['E_Potion'],name='Brew '+tech+'s Efficiently',inputs=inputs[identifier['E_Potion']],outputs=outputs[identifier['E_Potion']],maintenance=maintenance[i])
            #Upgrade to Efficient Cauldron
            outputRecipes[identifier['U_E_Potion']]=[recipes[identifier['E_Potion']]]
            recipes[identifier['U_E_Potion']]=Recipe(identifier=identifier['U_E_Potion'],name='Upgrade to Producing '+tech+'s Efficiently',inputs=inputs[identifier['U_E_Potion']],outputRecipes=outputRecipes[identifier['U_E_Potion']],useOnce=True)
            #Make Upgrade Cauldron recipe
            outputRecipes[identifier['U_Potion']]=[recipes[identifier['Potion']],recipes[identifier['U_E_Potion']]]
            if i!=len(techs)-1:
                outputRecipes[identifier['U_Potion']].append(recipes['U_'+techs[i+1].replace(" ", "")+str(j)])
            if i!=0:
                recipes[identifier['U_Potion']]=Recipe(identifier=identifier['U_Potion'],name='Upgrade to '+tech+'s',inputs=inputs[identifier['U_Potion']],outputRecipes=outputRecipes[identifier['U_Potion']],useOnce=True)

            
    factories['SilverCauldron']=Factory(identifier='SilverCauldron',name='Silver Cauldron',fuel=ItemStack(name='Stick'),inputs=[ItemStack(name='Bowl',material='BOWL')],maintenanceInputs=[ItemStack(name='Charcoal',material='STICK')])
    factories['SilverCauldron'].addRecipe(recipes[techs[0].replace(" ", "")+'0'])
    factories['SilverCauldron'].addRecipe(recipes['U_'+techs[1].replace(" ", "")+'0'])
    factories['SilverCauldron'].addRecipe(recipes['U_E_'+techs[0].replace(" ", "")+'0']) 
    factories['BlackCauldron']=Factory(identifier='BlackCauldron',name='Black Cauldron',fuel=ItemStack(name='Stick'),inputs=[ItemStack(name='Bowl',material='BOWL',amount=2)],maintenanceInputs=[ItemStack(name='Charcoal',material='STICK')])
    factories['BlackCauldron'].addRecipe(recipes[techs[0].replace(" ", "")+'1'])
    factories['BlackCauldron'].addRecipe(recipes['U_'+techs[1].replace(" ", "")+'1'])
    factories['BlackCauldron'].addRecipe(recipes['U_E_'+techs[0].replace(" ", "")+'1'])
    
    return {'factories':factories,'recipes':recipes}    
 
if __name__ == '__main__':
    main()